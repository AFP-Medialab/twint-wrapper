package com.afp.medialab.weverify.social.twint;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
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
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.query.BulkOptions;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;
import org.springframework.stereotype.Component;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.model.twint.WordsInTweet;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Transactional
public class ESOperations {

	@Autowired
	private ElasticsearchOperations esOperation;

	@Autowired
	private TweetsPostProcess twintModelAdapter;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static Logger Logger = LoggerFactory.getLogger(ESOperations.class);

	/**
	 * Search query with essid
	 * 
	 * @deprecated
	 * @param essid
	 * @param start
	 * @param end
	 * @throws IOException
	 */
	public void enrichWithTweetie(String essid, String start, String end) throws IOException {
		QueryBuilder builder = boolQuery().must(matchQuery("essid", essid));
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).withPageable(PageRequest.of(0, 10))
				.build();
		ScrolledPage<TwintModel> scroll = esOperation.startScroll(1000, searchQuery, TwintModel.class);
		String scrollId = scroll.getScrollId();
		List<TwintModel> model = new ArrayList<TwintModel>();
		while (scroll.hasContent()) {
			model.addAll(scroll.getContent());
			scrollId = scroll.getScrollId();
			scroll = esOperation.continueScroll(scrollId, 1000, TwintModel.class);
		}
		indexWordsObj(model);
	}

	/**
	 * Get Twint request from ES that match with the current collectRequest and with
	 * tweets without wit fields.
	 * 
	 * @param collectRequest
	 * @return
	 * @throws IOException
	 */
	public List<TwintModel> enrichWithTweetie(CollectRequest collectRequest) throws IOException {

		BoolQueryBuilder builder = searchQueryBuilder(collectRequest);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).withPageable(PageRequest.of(0, 10))
				.build();
		ScrolledPage<TwintModel> scroll = esOperation.startScroll(1000, searchQuery, TwintModel.class);
		String scrollId = scroll.getScrollId();
		List<TwintModel> model = new ArrayList<TwintModel>();
		while (scroll.hasContent()) {
			model.addAll(scroll.getContent());
			scrollId = scroll.getScrollId();
			scroll = esOperation.continueScroll(scrollId, 1000, TwintModel.class);
		}
		return model;
	}

	/**
	 * Build ES query for current search
	 * @param collectRequest
	 * @return
	 */
	private BoolQueryBuilder searchQueryBuilder(CollectRequest collectRequest) {
		String keywordQuery = TwintRequestGenerator.getInstance().generateSearch(collectRequest);
		BoolQueryBuilder builder = QueryBuilders.boolQuery();
		builder.must(matchPhraseQuery("search", keywordQuery));
		builder.mustNot(existsQuery("wit"));
		Set<String> users = collectRequest.getUserList();
		if (users != null && !users.isEmpty()) {
			QueryBuilder userQueryBuilder = new TermsQueryBuilder("username", users);
			builder.must(userQueryBuilder);
		}
		if (!collectRequest.isDisableTimeRange()) {
			String from = dateFormat.format(collectRequest.getFrom());
			String until = dateFormat.format(collectRequest.getUntil());
			Logger.debug("search from {} to {}", from, until);

			builder.filter(
					rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis").gte(from).lte(until));
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

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build()
				.addSort(Sort.by("date").ascending()).setPageable(PageRequest.of(0, 1));
		final List<TwintModel> hits = esOperation.queryForList(searchQuery, TwintModel.class);

		if (hits.size() < 1) {
			Logger.error("No tweets found");
			return null;
		}
		TwintModel twintModel = hits.get(0);
		return twintModel.getDate();

	}

	/**
	 * Add Twitie data
	 * 
	 * @param tms Indexed document
	 * @throws IOException
	 */
	public void indexWordsObj(List<TwintModel> tms) throws IOException {
		List<UpdateQuery> updateQueries = new LinkedList<UpdateQuery>();

		int i = 0;
		boolean allNull = true;
		Logger.info("call Twittie WS for {} extracted tweets", tms.size());
		for (TwintModel tm : tms) {

			try {
				allNull = false;
				// Logger.debug("Builtin wit : " + i++ + "/" + tms.size());
				List<WordsInTweet> wit = twintModelAdapter.buildWit(tm.getTweet(), tm.getSearch());

				ObjectMapper mapper = new ObjectMapper();
				String b = "{\"wit\": " + mapper.writeValueAsString(wit) + "}";

				UpdateQuery updateQuery = new UpdateQueryBuilder().withId(tm.getId()).withClass(TwintModel.class)
						.build();

				UpdateRequest updateRequest = new UpdateRequest();

				updateRequest.doc(b, XContentType.JSON);
				updateQuery.setUpdateRequest(updateRequest);
				updateQueries.add(updateQuery);

				i++;
			} catch (Exception e) {
				Logger.error("Error processing this tweet: {} with error : {}", tm.getId(), e.getMessage());
				// e.printStackTrace();
			}
		}

		Logger.debug("{}/{} process  tweets ", i, tms.size());
		if (!allNull)
			esOperation.bulkUpdate(updateQueries, BulkOptions.defaultOptions());

	}

}
