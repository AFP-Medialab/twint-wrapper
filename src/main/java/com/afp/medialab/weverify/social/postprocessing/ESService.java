package com.afp.medialab.weverify.social.postprocessing;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.text.SimpleDateFormat;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;

@Service
public class ESService {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private ElasticsearchOperations esOperation;

	public List<TwintModel> getTweet(CollectRequest request, String session, int count) {
		String sinceStr = dateFormat.format(request.getFrom());
		String untilStr = dateFormat.format(request.getUntil());

		QueryBuilder builder = boolQuery().must(matchQuery("essid", session)).filter(
				rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis").gte(sinceStr).lte(untilStr));
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build()
				.addSort(Sort.by("date").ascending()).setPageable(PageRequest.of(0, count));
		final List<TwintModel> hits = esOperation.queryForList(searchQuery, TwintModel.class);
		return hits;
	}

}
