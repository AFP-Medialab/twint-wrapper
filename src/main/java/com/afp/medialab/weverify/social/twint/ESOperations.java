package com.afp.medialab.weverify.social.twint;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Component
@Transactional
public class ESOperations {

    @Autowired
    private ElasticsearchOperations esOperation;


    @Autowired
    private ESConfiguration esConfiguration;

    @Autowired
    private TwintModelAdapter twintModelAdapter;


    private static Logger Logger = LoggerFactory.getLogger(TwintThread.class);


    public List<TwintModel> getModels(String essid, String start, String end) throws InterruptedException {

       // esOperation.refresh(TwintModel.class);
        QueryBuilder builder = boolQuery().must(matchQuery("essid", essid));
             //   .filter(rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
               //         .gte(start).lte(end));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build().setPageable(PageRequest.of(0, 10000));;
              //  .addSort(Sort.by("date").ascending());

        final List<TwintModel> model = esOperation.queryForList(searchQuery, TwintModel.class);
        System.out.println(model.size());
        return model;
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


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    public void indexWordsObj(List<TwintModel> tms) throws IOException, InterruptedException {
        BulkRequest requests = new BulkRequest();

        int i = 0;
        for (TwintModel tm : tms) {

            if (tm.getWit() == null) {
                try {
                    Logger.info("Builtin wit : " + i++ + "/" + tms.size());
                    twintModelAdapter.buildWit(tm);


                    ObjectMapper mapper = new ObjectMapper();
                    String b = "{\"wit\": " + mapper.writeValueAsString(tm.getWit()) + "}";

                    IndexRequest indexRequest = new IndexRequest("twinttweets");
                    indexRequest.id(tm.getId());
                    indexRequest.type("_doc");

                    UpdateRequest updateRequest = new UpdateRequest();
                    updateRequest.index("twinttweets");
                    updateRequest.type("_doc");
                    updateRequest.id(tm.getId());

                    updateRequest.doc(b, XContentType.JSON);

                    requests.add(updateRequest);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        BulkResponse response = esConfiguration.elasticsearchClient().bulk(requests);
       Logger.info(response.toString());

    }

}
