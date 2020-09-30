package com.afp.medialab.weverify.social.twint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.afp.medialab.weverify.social.model.twint.TwitieResponse;
import com.afp.medialab.weverify.social.model.twint.WordsInTweet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
/**
 * Call Tweetie service Count number of word occurrences with entity type if
 * detected by Tweetie
 * 
 * @author Bertrand Goupil
 *
 */
public class TweetsPostProcess {

	private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TweetsPostProcess.class);

	@Autowired
	@Qualifier("ESRestTempate")
	private RestTemplate restTemplate;

	@Value("${application.twitie.url}")
	private String twitieURL;

	private Map<String, List<String>> stopwords;
	private List<String> regExps;

	@Value("classpath:stopwords.json")
	private Resource stopWordsResource;

	@Value("classpath:regexp.txt")
	private Resource regexResource;

	private boolean twitieDown = false;

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

	private String getTweetLang(String tweet, String[] langs) {
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
	 * Call Tweetie Gate webservice
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<Tweetie> callTwitie(String tweet) throws IOException {

		// HTTPConnexion Timeout
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.postForEntity(twitieURL, tweet, String.class);

		} catch (HttpServerErrorException ex) {
			Logger.error("Fail calling TwitIE {}" , ex.getRawStatusCode());
			return null;
		}
		catch (Exception e) {
			Logger.error("Fail calling TwitIE - TwitIE is down");
			twitieDown = true;
			return null;
		}
		// Logger.debug("SUCCESSFULLY CALLED TwitIE");
		ObjectMapper mapper = new ObjectMapper();

		JsonNode root = mapper.readTree(response.getBody());
		JsonNode annotations = root.get("response").get("annotations");

		TwitieResponse twitieResponse = mapper.treeToValue(annotations, TwitieResponse.class);
		List<Tweetie> tweeties = new LinkedList<Tweetie>();

		twitieResponse.getPerson().forEach(p -> {
			String per = p.getFeatures().getString();
			String n_per = per.toLowerCase().replaceAll(" |/.", "_");
			Tweetie tweetie = new Tweetie(per, n_per, "Person");
			tweeties.add(tweetie);
			// tweetTweetie = tweet.replaceAll(per, n_per);

			// tokenJSON.put(n_per, "Person");
		});
		twitieResponse.getUserID().forEach(p -> {
			String feat = "@" + p.getFeatures().getString();
			// String norm = p.getFeatures().getString().toLowerCase().replaceAll("@", "");
			String norm = "@" + p.getFeatures().getString().toLowerCase();
			// tweet = tweet.replaceAll(p.getFeatures().getString(),
			// p.getFeatures().getString().toLowerCase().replaceAll("@", ""));
			Tweetie tweetie = new Tweetie(feat, norm, "UserID");
			tweeties.add(tweetie);
			// tokenJSON.put("@" + p.getFeatures().getString().toLowerCase(), "UserID");
		});
		twitieResponse.getLocation().forEach(p -> {
			String per = p.getFeatures().getString();
			String n_per = per.toLowerCase().replaceAll(" |/.", "_");
			Tweetie tweetie = new Tweetie(per, n_per, "Location");
			tweeties.add(tweetie);
			// tweet = tweet.replaceAll(per, n_per);
			// tokenJSON.put(n_per, "Location");
		});
		twitieResponse.getOrganization().forEach(p -> {
			String per = p.getFeatures().getString();
			String n_per = per.toLowerCase().replaceAll(" |/.", "_");
			Tweetie tweetie = new Tweetie(per, n_per, "Organization");
			tweeties.add(tweetie);
			// tweet = tweet.replaceAll(per, n_per);
			// tokenJSON.put(n_per, "Organization");
		});

		return tweeties;
	}

	public List<WordsInTweet> buildWit(String tweet) throws InterruptedException, ParseException, IOException {
		List<WordsInTweet> wit = new ArrayList<>();
		tweet = StringUtils.normalizeSpace(tweet);
		if (tweet.isEmpty())
			return wit;
		List<Tweetie> tweeties;
		Map<String, String> tokenJSON = new HashMap<>();
		if (!twitieDown) {
			tweeties = callTwitie(tweet);
			for (Tweetie tweetie : tweeties) {
				tweet = tweet.replaceAll(tweetie.getFeature(), tweetie.getNormalized());
				tokenJSON.put(tweetie.getNormalized(), tweetie.getEntity());
			}
		}

		String[] langs = new String[] { "fr", "en" };

		List<String> stopLang = stopwords.get(getTweetLang(tweet, langs));
		// List<String> stopGlob = stopwords.get("glob");

		// stopGlob.addAll(Arrays.asList(search.replaceAll("#", "").split(" ")));
		// String tweet = tm.getTweet();

		for (String regExp : regExps) {
			tweet = tweet.replaceAll(regExp, " ");
		}
		tweet = StringUtils.normalizeSpace(tweet);
		List<String> words = Arrays.asList(tweet.toLowerCase().split("\\s+"));

		Map<String, Integer> occurences = new HashMap<>();

		words.stream().forEach((word) -> {

			// if (!stopLang.contains(word) && !stopGlob.contains(word) && !word.equals("
			// "))
			if (!stopLang.contains(word) && !word.equals(" "))
				if (occurences.get(word) == null)
					occurences.put(word, 1);
				else
					occurences.put(word, occurences.get(word) + 1);
		});

		occurences.forEach((word, occ) -> {
			WordsInTweet w = new WordsInTweet();
			w.setWord(word);
			w.setNbOccurences(occ);
			w.setEntity((tokenJSON.get(word) != null) ? tokenJSON.get(word) : null);

			wit.add(w);
		});

		return wit;
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
}

class Tweetie {
	private String feature;
	private String normalized;
	private String entity;

	public Tweetie(String feature, String normalized, String entity) {
		this.feature = feature;
		this.normalized = normalized;
		this.entity = entity;
	}

	public String getFeature() {
		return feature;
	}

	public String getNormalized() {
		return normalized;
	}

	public String getEntity() {
		return entity;
	}
}
