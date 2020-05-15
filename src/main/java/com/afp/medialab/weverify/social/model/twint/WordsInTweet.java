package com.afp.medialab.weverify.social.model.twint;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WordsInTweet {
	private String word;
	private int nbOccurences;
	private String entity;

	/*
	 * public WordsInTweet(String word, int nbOccurences, String entity) { this.word
	 * = word; this.nbOccurences = nbOccurences; this.entity = entity; }
	 */
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