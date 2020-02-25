package com.afp.medialab.weverify.social;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.afp.medialab.weverify.social.controller.TwitterGatewayServiceController;

@RunWith(SpringRunner.class)
@WebMvcTest(TwitterGatewayServiceController.class)
public class TwitterGatewayApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Test
	public void contextLoads() throws Exception {
//		String collect = "{\n" + 
//				"    \"search\": {\n" + 
//				"        \"search\": \"#fake\"\n" + 
//				"    },\n" + 
//				"    \"from\": \"2019-09-01\",\n" + 
//				"    \"until\": \"2019-09-04\",\n" + 
//				"    \"lang\": \"fr\"\n" + 
//				"}";
		this.mvc.perform(get("/")).andExpect(status().isOk());
//		this.mvc.perform(post("/collect").contentType(MediaType.APPLICATION_JSON_VALUE)
//				.content(collect))
//				.andExpect(status().isOk());
	}

}
