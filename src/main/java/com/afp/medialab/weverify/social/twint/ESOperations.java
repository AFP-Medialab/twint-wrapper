package com.afp.medialab.weverify.social.twint;

import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.Transactional;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.model.twint.WordsInTweet;
import com.afp.medialab.weverify.social.util.AlwaysBlockingSynchronousQueue;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class ESOperations {

	@Autowired
	private ElasticsearchOperations esOperation;

	@Autowired
	private TweetsPostProcess twintModelAdapter;

	@Autowired
	RestHighLevelClient highLevelClient;

	@Value("${application.twitie.threads}")
	private int twitieThreads;

	@Value("${application.twitie.es.bulksize}")
	private int bulksize;

	@Value("${application.twitie.es.pagesize}")
	private int pagesize;

	private static Logger Logger = LoggerFactory.getLogger(ESOperations.class);

	/**
	 * Search query with essid
	 * 
	 * @param essid
	 * @param start
	 * @param end
	 * @throws IOException
	 */
	public void enrichWithTweetie(String essid) throws IOException {
		BoolQueryBuilder builder = QueryBuilders.boolQuery();
		builder.must(matchQuery("essid", essid));
		builder.mustNot(existsQuery("wit"));
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder)
				.withPageable(PageRequest.of(0, pagesize)).build();

		// an AtomicInteger so that we can count the number of successfully processed
		// Tweets
		// from within the code running on multiple threads
		AtomicInteger successful = new AtomicInteger();

		// use a try with resources to ensure the iterator is closed no matter what
		try (SearchHitsIterator<TwintModel> stream = esOperation.searchForStream(searchQuery, TwintModel.class)) {

			// create an ObjectMapper for conversion to JSON
			final ObjectMapper mapper = new ObjectMapper();

			// dedicated thread to send the results to Elastic in batches
			ArrayBlockingQueue<UpdateRequest> q = new ArrayBlockingQueue<>(2000);
			UpdateRequest doneSignal = new UpdateRequest(null, null); // marker
			Thread queueConsumer = new Thread(() -> {
				List<DocWriteRequest<?>> requests = new ArrayList<>();
				UpdateRequest r = null;
				try {
					while ((r = q.take()) != doneSignal) {
						requests.add(r);
						if (requests.size() >= bulksize) {
							try {
								highLevelClient.bulk(new BulkRequest("tsnatweets").add(requests),
										RequestOptions.DEFAULT);
								successful.addAndGet(requests.size());
							} catch (Exception e) {
								Logger.error("Error sending updates to Elastic", e);
							}
							requests.clear();
						}
					}
				} catch (InterruptedException e) {
					// finished
				}
				if (requests.size() > 0) {
					try {
						highLevelClient.bulk(new BulkRequest("tsnatweets").add(requests), RequestOptions.DEFAULT);
						successful.addAndGet(requests.size());
					} catch (Exception e) {
						Logger.error("Error sending updates to Elastic", e);
					}
				}
			});
			queueConsumer.start();

			// thread pool to call TwitIE - AlwaysBlockingSynchronousQueue ensures that we
			// don't fetch
			// tweets from Elastic faster than the TwitIE service can process them, which
			// should avoid
			// timing out the scroller
			ExecutorService twitieExecutor = new ThreadPoolExecutor(twitieThreads, twitieThreads, 0L,
					TimeUnit.MILLISECONDS, new AlwaysBlockingSynchronousQueue());

			// now we work our way through all the hits
			stream.forEachRemaining(hit -> {
				// get the TwintModel object out of the ES search result hit
				TwintModel tm = hit.getContent();

				twitieExecutor.execute(() -> {
					try {
						// this is the time consuming bit that eventually runs TwitIE
						// but now we are using multiple threads this should be a bit quicker
						List<WordsInTweet> wit = twintModelAdapter.buildWit(tm.getFull_text());

						// convert the result of running TwitIE into a JSON version
						String b = "{\"wit\": " + mapper.writeValueAsString(wit) + "}";

						// build a request to update the Tweet with the info from TwitIE
						UpdateRequest updateRequest = new UpdateRequest("tsnatweets", tm.getId());
						updateRequest.doc(b, XContentType.JSON);

						// pass the update to ES
						q.add(updateRequest);
					} catch (Exception e) {
						Logger.error("Error processing this tweet: {} with error : {}", tm.getId(), e.getMessage());
					}
				});
			});

			twitieExecutor.shutdown();
			try {
				twitieExecutor.awaitTermination(10, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				Logger.warn("Interrupted while waiting for TwitIE executor to finish", e);
			}
			q.add(doneSignal);
			try {
				queueConsumer.join(10000);
			} catch (InterruptedException e) {
				Logger.warn("Interrupted waiting for TwitIE consumer to finish", e);
			}

			// and we are back to single threaded processing so let's log how
			// many of the tweets we pulled from ES that we managed to process
			Logger.debug("successfully processed {} of {} tweets", successful.get(), stream.getTotalHits());
		}
	}

	/**
	 * Get Twint request from ES that match with the current collectRequest and with
	 * tweets without wit fields.
	 * 
	 * @deprecated
	 * @param collectRequest
	 * @return
	 * @throws IOException
	 */
	public List<TwintModel> enrichWithTweetie(CollectRequest collectRequest) throws IOException {

		BoolQueryBuilder builder = searchQueryBuilder(collectRequest);

		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder)
				.withPageable(PageRequest.of(0, 10)).build();
		SearchHitsIterator<TwintModel> stream = esOperation.searchForStream(searchQuery, TwintModel.class);

		List<TwintModel> model = new ArrayList<TwintModel>();
		while (stream.hasNext()) {
			model.add(stream.next().getContent());
		}
		stream.close();
		return model;
	}

	/**
	 * Build ES query for current search
	 * 
	 * @param collectRequest
	 * @return
	 */
	private BoolQueryBuilder searchQueryBuilder(CollectRequest collectRequest) {
		// String keywordQuery =
		// TwintRequestGenerator.getInstance().generateSearch(collectRequest);
		BoolQueryBuilder builder = QueryBuilders.boolQuery();
		// builder.must(matchPhraseQuery("search", keywordQuery));
		builder.mustNot(existsQuery("wit"));
		Set<String> users = collectRequest.getUserList();
		if (users != null && !users.isEmpty()) {
			QueryBuilder userQueryBuilder = new TermsQueryBuilder("screen_name", users);
			builder.must(userQueryBuilder);
		}
		if (!collectRequest.isDisableTimeRange()) {
			long fromEpoch = collectRequest.getFrom().toInstant().getEpochSecond();
			long untilEpoch = collectRequest.getUntil().toInstant().getEpochSecond();
			// String from = dateFormat.format(collectRequest.getFrom());
			// String until = dateFormat.format(collectRequest.getUntil());
			// Logger.debug("search from {} to {}", from, until);
			Logger.debug("search from {} to {}", fromEpoch, untilEpoch);

			builder.filter(rangeQuery("datetimestamp").format("epoch_second").gte(fromEpoch).lte(untilEpoch));
		}
		return builder;
	}

	/**
	 * Get the latest tweet extracted for this session. (Oldest tweet is always
	 * searched)
	 *
	 * @param request
	 * @param session
	 * @return
	 */
	public Date findWhereIndexingStopped(CollectRequest request) {

		QueryBuilder builder = searchQueryBuilder(request);

		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build()
				.addSort(Sort.by("datetimestamp").ascending()).setPageable(PageRequest.of(0, 1));
		final SearchHits<TwintModel> hits = esOperation.search(searchQuery, TwintModel.class);

		if (hits.isEmpty()) {
			Logger.error("No tweets found");
			return null;
		}
		SearchHit<TwintModel> twintModelHit = hits.getSearchHit(0);
		TwintModel twintModel = twintModelHit.getContent();
		long datetimestamp = twintModel.getDatetimestamp();
		Instant instant = Instant.ofEpochSecond(datetimestamp);
		Date date = Date.from(instant);
		return date;

	}

}
