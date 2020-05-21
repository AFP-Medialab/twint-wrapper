package com.afp.medialab.weverify.social.model.twint;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "tsnatweets", type = "_doc")
public class TwintModel {

	@Id
	private String id;

	private long datetimestamp;
	
	private String full_text;
	
	@Field(name = "wit", type = FieldType.Nested, includeInParent = true)
	private List<WordsInTweet> wit;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFull_text() {
		return full_text;
	}

	public void setFull_text(String tweet) {
		this.full_text = tweet;
	}

	public List<WordsInTweet> getWit() {
		return wit;
	}

	public void setWit(List<WordsInTweet> wit) {
		this.wit = wit;
	}

	public long getDatetimestamp() {
		return datetimestamp;
	}

	public void setDatetimestamp(long datetimestamp) {
		this.datetimestamp = datetimestamp;
	}

	

}
