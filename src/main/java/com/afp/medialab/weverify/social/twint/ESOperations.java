package com.afp.medialab.weverify.social.twint;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.Console;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Component
@Transactional
public class ESOperations {

    @Autowired
    private ElasticsearchOperations esOperation;

    private static Logger Logger = LoggerFactory.getLogger(TwintThread.class);

    public List<TwintModel> getModels(String essid, String start, String end) {
        QueryBuilder builder = boolQuery().must(matchQuery("essid", essid))
                .filter(rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
                        .gte(start).lte(end));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build()
                .addSort(Sort.by("date").ascending());
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


    public void indexWordsObj(String session, String from, String until) throws IOException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Logger.info("INDEXING session "  + session);
        String sinceStr = from; //dateFormat.format(from);
        String untilStr = until; //dateFormat.format(until);

        List<TwintModel> hits =
                getModels(session, from, until);

        hits.forEach(hit -> {
          //  hit.setWit();
        });
       /* QueryBuilder builder = boolQuery().must(matchQuery("essid", session)).filter(
                rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis").gte(sinceStr).lte(untilStr));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build();

        final List<TwintModel> hits = esOperation.queryForList(searchQuery, TwintModel.class);

        PutMappingRequest req = new PutMappingRequest("twinttweets");
        XContentBuilder build = XContentFactory.jsonBuilder();
        build.startObject();
        {
            build.startObject("named_words");
            {
                build.field("word", "word");
                build.field("nbOccurences", 3);
                build.field("entity", "organization");
            }
            build.endObject();
        }
        build.endObject();
        req.source(build);
        hits.forEach(hit -> {
            UpdateRequest upReq = new UpdateRequest();
            upReq.index("twinttweets");
            upReq.type("_doc");
            upReq.id(hit.getId());
            try {
                upReq.doc(jsonBuilder()
                        .startObject()
                        .startObject("named_words")
                        .field("word", "SuperWord")
                        .endObject()
                        .endObject());
            } catch (IOException e) {
                e.printStackTrace();
            }
            UpdateQuery query = new UpdateQuery();
            query.setIndexName("twinttweets");
            query.setId(hit.getId());
            query.setType("_doc");
            query.setUpdateRequest(upReq);
            esOperation.update(query);
            esOperation.queryForList(searchQuery, TwintModel.class).forEach(tm -> {
            });
        });*/
    }

}
