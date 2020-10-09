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

import com.afp.medialab.weverify.social.model.twint.TwintModel;
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
		TwintModel twintModel = new TwintModel();
		String tweet = "I am in the White House, working hard. News reports concerning the Shutdown and Syria are mostly FAKE. We are negotiating with the Democrats on desperately needed Border Security (Gangs, Drugs, Human Trafficking &amp; more) but it could be a long stay. On Syria, we were originally...";
		twintModel.setFull_text(tweet);
		twintModel.setLang("en");
		List<WordsInTweet> wit = twintModelAdapter.buildWit(twintModel);
		ObjectMapper mapper = new ObjectMapper();
		String b = "{\"wit\": " + mapper.writeValueAsString(wit) + "}";
		System.out.println(b);
	}
}
