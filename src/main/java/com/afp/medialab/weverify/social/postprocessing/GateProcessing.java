package com.afp.medialab.weverify.social.postprocessing;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.twint.TwintModel;

@Service
public class GateProcessing {
	
	private static Logger Logger = LoggerFactory.getLogger(GateProcessing.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ESService esService;

	public void processTweetWithTwitIE(CollectRequest request, String session, int tweets) {
		List<TwintModel> hits = esService.getTweet(request, session, tweets);
		for (TwintModel tweet : hits) {
			
			HttpEntity<String> entity = buildEntity(tweet.getTweet());
			String response = restTemplate
					.postForObject("http://localhost:8081/process", entity, String.class);
			Logger.info(response);

		}
		Logger.debug("nb tweets: " + tweets);
		Logger.debug("nb search : " + hits.size());
	}

	private HttpEntity<String> buildEntity(String content) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		HttpEntity<String> entity = new HttpEntity<String>(content, headers);
		return entity;
	}
}
