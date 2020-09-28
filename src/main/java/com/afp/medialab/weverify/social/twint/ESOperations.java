package com.afp.medialab.weverify.social.twint;

import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.model.twint.WordsInTweet;

@Service
@Transactional
public class ESOperations {

	@Autowired
	private ElasticsearchOperations esOperation;
	
	@Autowired
	private TweetsPostProcess twintModelAdapter;

	@Autowired
	RestHighLevelClient highLevelClient;

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
				.withPageable(PageRequest.of(0, 10)).build();

		// an AtomicInteger so that we can count the number of successfully processed Tweets
		// from within the code running on multiple threads
		AtomicInteger successful = new AtomicInteger();

		// use a try with resources to ensure the iterator is closed no matter what
		try (SearchHitsIterator<TwintModel> stream = esOperation.searchForStream(searchQuery, TwintModel.class)) {

			// create a Spliterator over the normal ES iterator so we can work on the
			// elements in parallel. Specifying the size ensures that the data is split
			// sensibly across multiple threads and we can start calling TwitIE while
			// we are still pulling results from ES		
			Spliterator<SearchHit<TwintModel>> it = Spliterators.spliterator(stream,
											stream.getTotalHits(),
											Spliterator.IMMUTABLE | Spliterator.CONCURRENT);

			// create an ObjectMapper for conversion to JSON
			final ObjectMapper mapper = new ObjectMapper();

			// now we work our way through all the hits allowing the JVM to work
			// out how many threads we should use given where it's being run etc.
			StreamSupport.stream(it,true).forEach(hit -> {

				// get the TwintModel object out of the ES search result hit
				TwintModel tm = hit.getContent();

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
					highLevelClient.update(updateRequest, RequestOptions.DEFAULT);
					
					// if we've got this far we've successfully processed the tweet
					// and stored the result so update the counter
					successful.incrementAndGet();			
				} catch (Exception e) {
					Logger.error("Error processing this tweet: {} with error : {}", tm.getId(), e.getMessage());
				}

			});

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
