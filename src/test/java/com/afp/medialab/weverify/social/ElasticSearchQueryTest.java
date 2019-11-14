package com.afp.medialab.weverify.social;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.io.IOException;
import java.util.*;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.twint.ESOperations;
import com.afp.medialab.weverify.social.twint.TwintModelAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.twint.ESConfiguration;

@SpringBootTest
@RunWith(SpringRunner.class)
@ComponentScan("com.afp.medialab.weverify.social.twint")
public class ElasticSearchQueryTest {

	@Autowired
	private ESOperations esOperations;

	@Autowired
	private ElasticsearchOperations esOperation;


	@Autowired
	private ESConfiguration esConfiguration;

	@Test
	public void lastInsert() {
		String sessid = "1cae3a5c-6ca7-4442-a2d3-d3435e04d743";
		List<TwintModel> tms =
				esOperations.getModels(sessid, "2018-10-05","2018-12-13");

		esOperations.indexWordsObj(tms);

		esOperation.refresh(TwintModel.class);

		esOperations.getModels(sessid, "2018-10-05","2018-12-13").forEach(model -> System.out.println(model.getWit()));
	}

	@Test
	public void wordsInTweet(){

	}
}
