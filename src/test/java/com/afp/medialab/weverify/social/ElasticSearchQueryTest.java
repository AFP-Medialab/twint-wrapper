package com.afp.medialab.weverify.social;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.io.IOException;
import java.util.List;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.twint.ESOperations;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.parser.ParseException;
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
	private ESOperations esOperation;


	@Test
	public void lastInsert() throws IOException, InterruptedException, ParseException {
		//esOperation.indexWordsObj("a99ee7f1-13f9-4d10-9715-b93a027c33ed", "2018-10-10 20:50:00","2019-10-30 21:50:00");
		esOperation.getModels("a99ee7f1-13f9-4d10-9715-b93a027c33ed", "2018-10-10 20:50:00","2019-10-30 21:50:00").forEach(model-> {
			try {
				model.buildWit();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});//System.out.println(model.toString()));

	}

	@Test
	public void wordsInTweet(){

	}
}
