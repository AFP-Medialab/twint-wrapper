package com.afp.medialab.weverify.social;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.twint.ESConfiguration;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ESConfiguration.class)
public class ElasticSearchQueryTest {

	@Autowired
	private ElasticsearchOperations esOperation;

	@Test
	public void lastInsert() {
		QueryBuilder builder = boolQuery().must(matchQuery("essid", "2bfc8a8f-2221-462b-9d47-e969b9d8877c"))
				.filter(rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
						.gte("2019-09-29 12:00:00").lte("2019-10-03 00:00:00"));

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build()
				.addSort(Sort.by("date").ascending()).setPageable(PageRequest.of(0, 1));
		final List<TwintModel> model = esOperation.queryForList(searchQuery, TwintModel.class);

		System.out.println("ok " + model.size());
	}
}
