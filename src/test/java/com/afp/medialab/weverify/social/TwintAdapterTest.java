package com.afp.medialab.weverify.social;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.twint.TweetsPostProcess;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@RunWith(SpringRunner.class)
@ComponentScan("com.afp.medialab.weverify.social.twint")
public class TwintAdapterTest {

	@Autowired
	private TweetsPostProcess twintModelAdapter;
	
	@Test
	public void testTweetie() throws InterruptedException, ParseException, IOException {
		
		String tweet = "Jake Tapper of Fake News CNN @cnn just got destroyed in his interview with Stephen Miller of the Trump Administration. Watch the hatred and unfairness of this CNN flunky!";
		
		List<TwintModel.WordsInTweet> wit = twintModelAdapter.buildWit(tweet, "Fake News");
		ObjectMapper mapper = new ObjectMapper();
		String b = "{\"wit\": " + mapper.writeValueAsString(wit) + "}";
		System.out.println(b);
	}
}
