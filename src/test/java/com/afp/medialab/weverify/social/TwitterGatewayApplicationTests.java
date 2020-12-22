package com.afp.medialab.weverify.social;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.twint.TwintPlusRequestBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
public class TwitterGatewayApplicationTests {

//	@Autowired
//	private MockMvc mvc;

	@Test
	public void contextLoads() throws Exception {
		String collect = "{\n" + 
				"    \"keywordList\": [\n" + 
				"        \"'fake news'\"\n" + 
				"    ],\n" + 
				"    \"bannedWords\": [\"'fox news'\"],\n" + 
				"    \"lang\": \"en\",\n" + 
				"    \"from\": \"2016-12-01 00:00:00\",\n" + 
				"    \"until\": \"2020-04-21 00:00:00\",\n" + 
				"    \"userList\": [\"realDonaldTrump\"],\n" + 
				"    \"verified\": false,\n" + 
				"    \"media\": null,\n" + 
				"    \"retweetsHandling\": null\n" + 
				"}";
		ObjectMapper mapper = new ObjectMapper();
		CollectRequest cr = mapper.readValue(collect, CollectRequest.class);
		String request = TwintPlusRequestBuilder.getInstance().generateRequest(cr, "123", true, "host.docker.internal:9200", 20000);
		System.out.println(request);
		//this.mvc.perform(get("/")).andExpect(status().isOk());
//		this.mvc.perform(post("/collect").contentType(MediaType.APPLICATION_JSON_VALUE)
//				.content(collect))
//				.andExpect(status().isOk());
	}

}
