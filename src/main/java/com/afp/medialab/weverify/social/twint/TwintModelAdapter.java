package com.afp.medialab.weverify.social.twint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.model.twint.TwittieResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class TwintModelAdapter {

	private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TwintModelAdapter.class);

	@Autowired
	@Qualifier("ESRestTempate")
	private RestTemplate restTemplate;

	@Value("${application.twittie.url}")
	private String twitieURL;

	private Map<String, List<String>> stopwords;
	private List<String> regExps;

	private String tweet;

	@Value("classpath:stopwords.json")
	private Resource stopWordsResource;

	@Value("classpath:regexp.txt")
	private Resource regexResource;
	
	private boolean twittieDown = false;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void stopWordsFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		InputStream stopWords = stopWordsResource.getInputStream();
		stopwords = mapper.readValue(stopWords, Map.class);
		stopWords.close();
	}

	@PostConstruct
	public void regExps() throws IOException {
		InputStream regextinputstream = regexResource.getInputStream();
		InputStreamReader reader = new InputStreamReader(regextinputstream);
		regExps = new ArrayList<>();
		BufferedReader br = new BufferedReader(reader);
		String str;
		while ((str = br.readLine()) != null)
			regExps.add(str);
		br.close();
		regextinputstream.close();
		reader.close();
	}

	private String getTweetLang(String[] langs) {
		Map<String, Float> percents = new TreeMap<>();

		Arrays.stream(langs).forEach(lang -> {
			percents.put(lang, 0f);
			for (String stopword : stopwords.get(lang)) {
				String word = stopword;
				if (tweet.toLowerCase().contains(" " + word + " "))
					percents.put(lang, percents.get(lang) + 1);
			}
			percents.put(lang, percents.get(lang) / (stopwords.get(lang)).size());
		});

		return percents.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
				.get().getKey();

	}

	/**
	 * Call Twittie Gate webservice
	 * @return
	 * @throws IOException
	 */
	private Map<String, String> callTwittie() throws IOException {

		// HTTPConnexion Timeout
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.postForEntity(twitieURL, tweet, String.class);

		} catch (Exception e) {
			Logger.error("FAILED CALLING TWITTIE");
			twittieDown = true;
			return null;
		}
		Logger.debug("SUCCESSFULLY CALLED TWITTIE");
		ObjectMapper mapper = new ObjectMapper();

		JsonNode root = mapper.readTree(response.getBody());
		JsonNode annotations = root.get("response").get("annotations");

		TwittieResponse twittieResponse = mapper.treeToValue(annotations, TwittieResponse.class);
		Map<String, String> tokenJSON = new HashMap<>();

		twittieResponse.getPerson().forEach(p -> {
			String per = p.getFeatures().getString();
			String n_per = per.toLowerCase().replaceAll(" |/.", "_");
			tweet = tweet.replaceAll(per, n_per);

			tokenJSON.put(n_per, "Person");
		});
		twittieResponse.getUserID().forEach(p -> {
			tweet = tweet.replaceAll(p.getFeatures().getString(),
					p.getFeatures().getString().toLowerCase().replaceAll("@", ""));
			tokenJSON.put("@" + p.getFeatures().getString().toLowerCase(), "UserID");
		});
		twittieResponse.getLocation().forEach(p -> {
			String per = p.getFeatures().getString();
			String n_per = per.toLowerCase().replaceAll(" |/.", "_");
			tweet = tweet.replaceAll(per, n_per);
			tokenJSON.put(n_per, "Location");
		});
		twittieResponse.getOrganization().forEach(p -> {
			String per = p.getFeatures().getString();
			String n_per = per.toLowerCase().replaceAll(" |/.", "_");
			tweet = tweet.replaceAll(per, n_per);
			tokenJSON.put(n_per, "Organization");
		});

		return tokenJSON;
	}
	
	public void buildWit(TwintModel tm) throws InterruptedException, ParseException, IOException {

		tweet = tm.getTweet();
		tweet = StringUtils.normalizeSpace(tweet);
		Map<String, String> tokensNamed;
		if (!twittieDown)
			tokensNamed = callTwittie();
		else
			tokensNamed = null;

		String[] langs = new String[] { "fr", "en" };

		List<String> stopLang = stopwords.get(getTweetLang(langs));
		List<String> stopGlob = stopwords.get("glob");

		stopGlob.addAll(Arrays.asList(tm.getSearch().replaceAll("#", "").split(" ")));
		// String tweet = tm.getTweet();

		for (String regExp : regExps) {
			tweet = tweet.replaceAll(regExp, " ");
		}
		tweet = StringUtils.normalizeSpace(tweet);
		List<String> words = Arrays.asList(tweet.toLowerCase().split("\\s+"));

		Map<String, Integer> occurences = new HashMap<>();

		words.stream().forEach((word) -> {

			if (!stopLang.contains(word) && !stopGlob.contains(word) && !word.equals(" "))
				if (occurences.get(word) == null)
					occurences.put(word, 1);
				else
					occurences.put(word, occurences.get(word) + 1);
		});

		List<TwintModel.WordsInTweet> wit = new ArrayList<>();

		occurences.forEach((word, occ) -> {
			TwintModel.WordsInTweet w = new TwintModel.WordsInTweet();
			w.setWord(word);
			w.setNbOccurences(occ);
			w.setEntity((tokensNamed != null && tokensNamed.get(word) != null) ? tokensNamed.get(word) : null);

			wit.add(w);
		});

		tm.setTwittieTweet(tweet);
		tm.setWit(wit);
	}

	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
		// Connect timeout
		clientHttpRequestFactory.setConnectTimeout(5_000);

		// Read timeout
		clientHttpRequestFactory.setReadTimeout(10_000);
		return new RestTemplate(clientHttpRequestFactory);
	}

	public String getTweet() {
		return tweet;
	}
}
