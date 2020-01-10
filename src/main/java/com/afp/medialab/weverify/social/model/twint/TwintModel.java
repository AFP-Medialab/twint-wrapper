package com.afp.medialab.weverify.social.model.twint;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

@Document(indexName = "twinttweets", type = "_doc")
public class TwintModel {

	@Id
	private String id;
	private String conversation_id;
	private Long create_at;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date date;
	private String timezone, place, tweet, twittieTweet;

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

	public String getTwittieTweet() {
		return twittieTweet;
	}

	public void setTwittieTweet(String twittieTweet) {
		this.twittieTweet = twittieTweet;
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

	/*public enum NamedEntity {
		Person,
		Organization,
		Location,
		UserID
	}*/

	public static class WordsInTweet {
		private String word;
		private int nbOccurences;
		private String entity;


		/*public WordsInTweet(String word, int nbOccurences, String entity)
		{
			this.word = word;
			this.nbOccurences = nbOccurences;
			this.entity = entity;
		}*/
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

		public String getEntity() {
			return entity;
		}

		public void setEntity(String entity) {
			this.entity = entity;
		}

	}

	/*public String toString() {
		return ("ID: " + getId() + "\nConversation ID: " + getConversation_id() + "\nEssID: " + getEssid() + "\ncreate at: " + getCreate_at() + "\nDate: " + getDate().toString() + "\ntweet" + getTweet()
				+ "\nUsername: " + getUsername() + "\nword in tweets: " + wit);
	}*/

}
