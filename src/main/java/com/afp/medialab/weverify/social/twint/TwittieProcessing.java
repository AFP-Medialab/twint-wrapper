package com.afp.medialab.weverify.social.twint;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.model.twint.WordsInTweet;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TwittieProcessing {
	
	@Autowired
	RestHighLevelClient highLevelClient;

	@Autowired
	private TweetsPostProcess twintModelAdapter;
	
	private static Logger Logger = LoggerFactory.getLogger(TwittieProcessing.class);

	/**
	 * Add Twittie data
	 * 
	 * @param tms Indexed document
	 * @throws IOException
	 */
	@Async(value = "twittieCallTaskExecutor")
	public CompletableFuture<String> indexWordsObj(List<TwintModel> tms) throws IOException {

		int i = 0;
		Logger.info("call Twittie WS for {} extracted tweets", tms.size());
		BulkRequest bulkRequest = new BulkRequest();

		for (TwintModel tm : tms) {

			try {

				// Logger.debug("Builtin wit : " + i++ + "/" + tms.size());
				List<WordsInTweet> wit = twintModelAdapter.buildWit(tm.getFull_text());

				ObjectMapper mapper = new ObjectMapper();
				String b = "{\"wit\": " + mapper.writeValueAsString(wit) + "}";

				UpdateRequest updateRequest = new UpdateRequest("tsnatweets", tm.getId());
				updateRequest.doc(b, XContentType.JSON);

				bulkRequest.add(updateRequest);
				i++;
			} catch (Exception e) {
				Logger.error("Error processing this tweet: {} with error : {}", tm.getId(), e.getMessage());
				// e.printStackTrace();
			}
		}
		Logger.debug("{}/{} process  tweets ", i, tms.size());
		// SocketTimeoutException
		bulkQuery(bulkRequest);
		Logger.debug("Update success");
		return CompletableFuture.completedFuture("ok");
	}

	private void bulkQuery(BulkRequest bulkRequest) {
		try {
			highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			Logger.error("Error in Bulk request {} {}", e.getMessage(), e.getCause());
		}
	}
}
