package com.afp.medialab.weverify.social;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.junit4.SpringRunner;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.twint.ESOperations;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@RunWith(SpringRunner.class)
@ComponentScan("com.afp.medialab.weverify.social.twint")
public class ElasticSearchQueryTest {

	@Autowired
	private ElasticsearchOperations esOperation;

	@Autowired
	private ESOperations esOperations;
	

	private static final String donalTrumpQuery = "{\"keywordList\":[\"fake news\"],\"bannedWords\":null,\"lang\":null,\"from\":\"2016-12-01 00:00:00\",\"until\":\"2020-03-24 00:00:00\",\"userList\":[\"realDonaldTrump\"],\"verified\":false,\"media\":null,\"retweetsHandling\":null}";
	private static final String fakenotdeepfake = "{\"keywordList\":[\"#fake\"],\"bannedWords\":[\"deepfake\"],\"lang\":\"fr\",\"from\":\"2020-03-01 00:00:00\",\"until\":\"2020-03-02 00:00:00\",\"verified\":false,\"media\":null,\"retweetsHandling\":null}";
	private static final String fake = "{\"keywordList\":[\"#fake\"],\"bannedWords\":null,\"lang\":null,\"from\":\"2020-03-01 00:00:00\",\"until\":\"2020-03-02 00:00:00\",\"verified\":false,\"media\":null,\"retweetsHandling\":null}";

	// @Test
	public void lastInsert() {
		QueryBuilder builder = boolQuery().must(matchQuery("essid", "e759073c-dd59-4b6e-be67-4419b2ead383"))
				.filter(rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis").gte("2018-10-05")
						.lte("2018-12-13"));

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build()
				.addSort(Sort.by("date").ascending()).setPageable(PageRequest.of(0, 10000));
		final List<TwintModel> model = esOperation.queryForList(searchQuery, TwintModel.class);

		System.out.println("ok " + model.size());
	}

	@Test
	public void testQueryCollectRequest() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		CollectRequest collectRequest = objectMapper.readValue(donalTrumpQuery, CollectRequest.class);

		List<TwintModel> models = esOperations.enrichWithTweetie(collectRequest);
		esOperations.indexWordsObj(models);
		System.out.println("ok " + models.size());

	}

	// @Test
	public void addWit() throws IOException, InterruptedException {
		String sessid = "e759073c-dd59-4b6e-be67-4419b2ead383";
		esOperations.enrichWithTweetie(sessid, "2018-10-05", "2018-12-13");
		esOperation.refresh(TwintModel.class);

		// esOperations.getModels(sessid, "2018-10-05","2018-12-13").forEach(model ->
		// System.out.println(model.getWit()));
	}
}
