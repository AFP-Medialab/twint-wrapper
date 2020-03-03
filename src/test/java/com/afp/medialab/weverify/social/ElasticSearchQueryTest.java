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

import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.twint.ESOperations;

@SpringBootTest
@RunWith(SpringRunner.class)
@ComponentScan("com.afp.medialab.weverify.social.twint")
public class ElasticSearchQueryTest {

	@Autowired
	private ElasticsearchOperations esOperation;

	@Autowired
	private ESOperations esOperations;

	@Test
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
	public void addWit() throws IOException, InterruptedException {
		String sessid = "e759073c-dd59-4b6e-be67-4419b2ead383";
		esOperations.enrichWithTweetie(sessid, "2018-10-05", "2018-12-13");
		esOperation.refresh(TwintModel.class);

		// esOperations.getModels(sessid, "2018-10-05","2018-12-13").forEach(model ->
		// System.out.println(model.getWit()));
	}
}
