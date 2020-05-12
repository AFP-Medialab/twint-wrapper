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

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
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
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.entity.Request;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Transactional
public class ESOperations {

	@Autowired
	private ElasticsearchOperations esOperation;

	@Autowired
	private ESConfiguration esConfiguration;

	@Autowired
	private TweetsPostProcess twintModelAdapter;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static Logger Logger = LoggerFactory.getLogger(ESOperations.class);

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
	 * TODO to remove if not used
	 * 
	 * @param collectRequest
	 * @param collectHistory
	 * @return
	 * @throws IOException
	 */
	public List<TwintModel> enrichWithTweetie(CollectRequest collectRequest, CollectHistory collectHistory)
			throws IOException {
		List<Request> requests = collectHistory.getRequests();
		List<TwintModel> tms = new LinkedList<TwintModel>();
		for (Request request : requests) {
			if (!request.getMerge()) {
				collectRequest.setFrom(request.getSince());
				collectRequest.setUntil(request.getUntil());
				List<TwintModel> twintModel = enrichWithTweetie(collectRequest);
				tms.addAll(twintModel);
			}
		}
		return tms;

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

		String keywordQuery = buildKeyWordList(collectRequest);
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
	 * Build request string for keyword list and bannedwords
	 * 
	 * @param collectRequest
	 * @return
	 */
	public String buildKeyWordList(CollectRequest collectRequest) {
		Set<String> keywordsList = collectRequest.getKeywordList();
		Set<String> bannedWord = collectRequest.getBannedWords();
		StringBuffer reqbuffer = new StringBuffer();
		build(keywordsList, reqbuffer, true);
		if (bannedWord != null)
			build(bannedWord, reqbuffer, false);
		String request = reqbuffer.toString().trim();
		return request;
	}

	/**
	 * Build ES request for keyword list and bannedwords
	 * 
	 * @param keywords
	 * @param reqbuffer
	 * @param isKeyword
	 */
	private void build(Set<String> keywords, StringBuffer reqbuffer, boolean isKeyword) {
		boolean isFirst = true;
		for (String keyword : keywords) {
			if (isFirst) {
				if (isKeyword)
					reqbuffer.append(keyword + " ");
				else
					reqbuffer.append("-" + keyword + " ");
				isFirst = false;
			} else {
				if (isKeyword)
					reqbuffer.append("AND " + keyword + " ");
				else
					reqbuffer.append("-" + keyword + " ");
			}
		}
	}

	/**
	 * Get the latest tweet extracted for this session. (Oldest tweet is always
	 * searched)
	 *
	 * @param request
	 * @param session
	 * @return
	 */
	public Date findWhereIndexingStopped(CollectRequest request, String session) {

		String sinceStr = dateFormat.format(request.getFrom());
		String untilStr = dateFormat.format(request.getUntil());

		QueryBuilder builder = boolQuery().must(matchQuery("essid", session)).filter(
				rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis").gte(sinceStr).lte(untilStr));
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

	public void indexWordsObj(List<TwintModel> tms) throws IOException {
		BulkRequest requests = new BulkRequest();

		int i = 0;
		boolean allNull = true;
		Logger.info("call Twittie WS for {} extracted tweets", tms.size());
		for (TwintModel tm : tms) {

			// if (tm.getWit() == null) {
			try {
				allNull = false;
				// Logger.debug("Builtin wit : " + i++ + "/" + tms.size());
				List<TwintModel.WordsInTweet> wit = twintModelAdapter.buildWit(tm.getTweet(), tm.getSearch());

				ObjectMapper mapper = new ObjectMapper();
				String b = "{\"wit\": " + mapper.writeValueAsString(wit) + "}";

				IndexRequest indexRequest = new IndexRequest("twinttweets");
				indexRequest.id(tm.getId());
				indexRequest.type("_doc");

				UpdateRequest updateRequest = new UpdateRequest();
				updateRequest.index("twinttweets");
				updateRequest.type("_doc");
				updateRequest.id(tm.getId());

				updateRequest.doc(b, XContentType.JSON);

				requests.add(updateRequest);
				i++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// }
		Logger.debug("{}/{} process  tweets ", i, tms.size());
		if (!allNull)
			esConfiguration.elasticsearchClient().bulk(requests, RequestOptions.DEFAULT);

	}

}
