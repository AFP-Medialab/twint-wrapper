package com.afp.medialab.weverify.social;

import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.afp.medialab.weverify.social.model.twint.WordsInTweet;
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
		
		List<WordsInTweet> wit = twintModelAdapter.buildWit(tweet);
		ObjectMapper mapper = new ObjectMapper();
		String b = "{\"wit\": " + mapper.writeValueAsString(wit) + "}";
		System.out.println(b);
	}
}
