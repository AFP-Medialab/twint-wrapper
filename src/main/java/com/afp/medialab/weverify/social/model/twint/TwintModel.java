package com.afp.medialab.weverify.social.model.twint;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.naming.Name;

@Document(indexName = "twinttweets", type = "_doc")
public class TwintModel {

	@Id
	private String id;
	private String conversation_id;
	private Long create_at;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date date;
	private String timezone, place, tweet;

	public String[] getHashtags() {
		return hashtags;
	}

	public void setHashtags(String[] hashtags) {
		this.hashtags = hashtags;
	}

	public String[] getCashtags() {
		return cashtags;
	}

	public void setCashtags(String[] cashtags) {
		this.cashtags = cashtags;
	}

	public String getUser_id_str() {
		return user_id_str;
	}

	public void setUser_id_str(String user_id_str) {
		this.user_id_str = user_id_str;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public boolean isRetweet() {
		return retweet;
	}

	public void setRetweet(boolean retweet) {
		this.retweet = retweet;
	}

	public String getEssid() {
		return essid;
	}

	public void setEssid(String essid) {
		this.essid = essid;
	}

	public int getNlikes() {
		return nlikes;
	}

	public void setNlikes(int nlikes) {
		this.nlikes = nlikes;
	}

	public int getNreplies() {
		return nreplies;
	}

	public void setNreplies(int nreplies) {
		this.nreplies = nreplies;
	}

	public int getNretweets() {
		return nretweets;
	}

	public void setNretweets(int nretweets) {
		this.nretweets = nretweets;
	}

	public String getQuote_url() {
		return quote_url;
	}

	public void setQuote_url(String quote_url) {
		this.quote_url = quote_url;
	}

	public int getVideo() {
		return video;
	}

	public void setVideo(int video) {
		this.video = video;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getNear() {
		return near;
	}

	public void setNear(String near) {
		this.near = near;
	}

	public String[] getPhotos() {
		return photos;
	}

	public void setPhotos(String[] photos) {
		this.photos = photos;
	}

	public String[] getVideos() {
		return videos;
	}

	public void setVideos(String[] videos) {
		this.videos = videos;
	}

	private String[] hashtags, cashtags;
	private String user_id_str, username, name;
	private int day;
	private String hour, link;

	private boolean retweet;
	private String essid;
	private int nlikes, nreplies, nretweets;
	private String quote_url;
	private int video;
	private String search, near;
	private String[] photos, videos;

	private List<WordsInTweet> wit;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getConversation_id() {
		return conversation_id;
	}

	public void setConversation_id(String conversation_id) {
		this.conversation_id = conversation_id;
	}

	public Long getCreate_at() {
		return create_at;
	}

	public void setCreate_at(Long create_at) {
		this.create_at = create_at;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getTweet() {
		return tweet;
	}

	public void setTweet(String tweet) {
		this.tweet = tweet;
	}

	public List<WordsInTweet> getWit() {
		return wit;
	}

	public void setWit(List<WordsInTweet> wit) {
		this.wit = wit;
	}

	private static String getTweetLang(String text, String[] langs, JSONObject stopwords) {
		Map<String, Float> percents = new TreeMap<>();
		Arrays.stream(langs).forEach(lang -> {
			percents.put(lang, 0f);
			for (Object stopword : (JSONArray) stopwords.get(lang)) {
				String word = (String) stopword;
				if (text.contains(" " + word + " "))
					percents.put(lang, percents.get(lang) + 1);
			}
			percents.put(lang, percents.get(lang) / ((JSONArray) stopwords.get(lang)).size());//Arrays.stream((Array) stopwords.get(lang))
		});
		return percents.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

	}

	private Map<String, NamedEntity> callTwittie(String tweet) throws IOException, InterruptedException, ParseException {
		System.out.println("TWEET: " + tweet);
		URL url = new URL("http://185.249.140.38/weverify-twitie/process?annotations=:Person,:UserID,:Location,:Organization");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "text/plain");

		OutputStream os = con.getOutputStream();
		os.write(tweet.getBytes());
		os.flush();

		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ con.getResponseCode());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(con.getInputStream())));


		String output;
		String fullOut = "";
		while ((output = br.readLine()) != null) {
			fullOut += output;
		}
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(fullOut);

		JSONObject annotations = (JSONObject) ((JSONObject) json.get("response")).get("annotations");
		Map<String, NamedEntity> tokenJSON = new HashMap<>();

		JSONArray orgaTwittieJSON = (JSONArray) annotations.get(":Organization");
		List<String> orgaJSON = new ArrayList<>();
		for (Object orga : orgaTwittieJSON)
		{
			String p = (String) ((JSONObject)((JSONObject) orga).get("features")).get("string");
			tokenJSON.put(p, NamedEntity.Organization);
		}

		JSONArray locTwittieJSON = (JSONArray) annotations.get(":Location");
		List<String> locJSON = new ArrayList<>();
		for (Object loc : locTwittieJSON)
		{
			String p = (String) ((JSONObject)((JSONObject) loc).get("features")).get("string");
			tokenJSON.put(p, NamedEntity.Location);
		}

		JSONArray personTwittieJSON = (JSONArray) annotations.get(":Person");
		List<String> personJSON = new ArrayList<>();
		for (Object person : personTwittieJSON)
		{
			String p = (String) ((JSONObject)((JSONObject) person).get("features")).get("string");
			tokenJSON.put(p, NamedEntity.Person);
		}

		JSONArray idsTwittieJSON = (JSONArray) annotations.get(":UserID");
		List<String> idsJSON = new ArrayList<>();
		for (Object id : idsTwittieJSON)
		{
			String p = (String) ((JSONObject)((JSONObject) id).get("features")).get("string");
			tokenJSON.put(p, NamedEntity.UserID);
		}

		con.disconnect();
		return tokenJSON;
	}

	public void buildWit() throws InterruptedException, ParseException, IOException {
		Map<String, NamedEntity> tokensNamed = callTwittie(tweet);
		JSONParser jp = new JSONParser();
		try (FileReader fr = new FileReader("src/main/resources/stopwords.json"))
		{
			Object obj =  jp.parse(fr);
			final JSONObject stopwords = (JSONObject) obj;

			String[] langs = new String[]{"fr", "en"};
			List<String> words = Arrays.asList(tweet.toLowerCase()
													.replaceAll("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)|pic\\.twitter\\.com\\/([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)", "")
													.replaceAll("[\\.\\(\\)0-9\\!\\?\\'\\’\\‘\\\"\\:\\,\\/\\\\\\%\\>\\<\\«\\»\\'\\#\\ \\;\\-\\&\\|]+|\\u00a9|\\u00ae|[\\u2000-\\u3300]|\\ud83c[\\ud000-\\udfff]|\\ud83d[\\ud000-\\udfff]|\\ud83e[\\ud000-\\udfff]", " ")
													.split(" "));

			Map<String, Integer> occurences = new HashMap<>();

			words.stream().forEach((word) -> {
				JSONArray stopLang = (JSONArray) stopwords.get(getTweetLang(tweet, langs, stopwords));
				JSONArray stopGlob = (JSONArray) stopwords.get("glob");

				if (!stopLang.contains(word) && !stopGlob.contains(word))
					if (occurences.get(word) == null)
						occurences.put(word, 1);
					else
						occurences.put(word, occurences.get(word)+1);
			});
			wit = new ArrayList<>();
			occurences.forEach((word, occ) -> wit.add(new WordsInTweet(word, occ, tokensNamed.get(word))));
			System.out.println("WORDS IN TWEET: " + wit);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	class ReplyTo {
		private String user_id, username;

		public String getUser_id() { return user_id; }

		public void setUser_id(String user_id) {
			this.user_id = user_id;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

	}

	public enum NamedEntity {
		Person,
		Organization,
		Location,
		UserID
	}

	class WordsInTweet {
		private String word;
		private int nbOccurences;
		private NamedEntity entity;

		public WordsInTweet(String word, int nbOccurences, NamedEntity entity)
		{
			this.word = word;
			this.nbOccurences = nbOccurences;
			this.entity = entity;
		}
		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			this.word = word;
		}

		public int getNbOccurences() {
			return nbOccurences;
		}


		public void setNbOccurences(int nbOccurences) {
			this.nbOccurences = nbOccurences;
		}

		public NamedEntity getEntity() {
			return entity;
		}

		public void setEntity(NamedEntity entity) {
			this.entity = entity;
		}

		public String toString() {
			return ("Word: " + word + "\nNb Occurences: " + nbOccurences + "\nEntity: " + entity + "\n");
		}
	}

	public String toString() {
		return ("ID: " + getId() + "\nConversation ID: " + getConversation_id() + "\nEssID: " + getEssid() + "\ncreate at: " + getCreate_at() + "\nDate: " + getDate().toString() + "\ntweet" + getTweet()
				+ "\nUsername: " + getUsername() + "\nword in tweets: " + wit);
	}

}
