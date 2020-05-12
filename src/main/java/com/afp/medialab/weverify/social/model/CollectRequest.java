package com.afp.medialab.weverify.social.model;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import com.afp.medialab.weverify.social.constrains.LangConstrain;
import com.afp.medialab.weverify.social.constrains.MediaConstrain;
import com.afp.medialab.weverify.social.constrains.RetweetHandlingConstrain;
import com.afp.medialab.weverify.social.dao.entity.Request;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_EMPTY)

public class CollectRequest {

	@NotNull(message = "keywords are mandatory")
	@JsonDeserialize(using = SetStringNormalizerDeserializer.class)
	private Set<String> keywordList;
	@JsonDeserialize(using = SetStringNormalizerDeserializer.class)
	private Set<String> bannedWords;

	@LangConstrain
	private String lang;
	@JsonDeserialize(using = SetStringNormalizerDeserializer.class)
	private Set<String> userList;
	@JsonProperty("from")
	@JsonDeserialize(using = MultiDateDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date from;
	@JsonProperty("until")
	@JsonDeserialize(using = MultiDateDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date until;

	@MediaConstrain
	private String media;

	private Boolean verified = false;
	
	private Boolean disableTimeRange = false;

	@RetweetHandlingConstrain
	private String retweetsHandling;

	public CollectRequest() {
	}

	public CollectRequest(CollectRequest collectRequest) {
		this.keywordList = collectRequest.keywordList;
		this.bannedWords = collectRequest.bannedWords;
		this.lang = collectRequest.lang;
		this.userList = collectRequest.userList;
		this.from = collectRequest.from;
		this.until = collectRequest.until;
		this.media = collectRequest.media;
		this.verified = collectRequest.verified;
		this.retweetsHandling = collectRequest.retweetsHandling;
		this.disableTimeRange = collectRequest.disableTimeRange;
	}

	public CollectRequest(Request request) {
		this.keywordList = request.getKeywordList();
		this.bannedWords = request.getBannedWords();
		this.lang = request.getLanguage();
		this.userList = request.getUserList();
		this.from = request.getSince();
		this.until = request.getUntil();
		this.media = request.getMedia();
		this.verified = request.getVerified();
		this.retweetsHandling = request.getRetweetsHandling();
	}

	public Set<String> getKeywordList() {
		return keywordList;
	}

	public void setKeywordList(SortedSet<String> keywordList) {
		this.keywordList = keywordList;
	}

	public Set<String> getBannedWords() {
		return bannedWords;
	}

	public void setBannedWords(SortedSet<String> bannedWords) {
		this.bannedWords = bannedWords;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getUntil() {
		return until;
	}

	public void setUntil(Date until) {
		this.until = until;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Set<String> getUserList() {
		return userList;
	}

	public void setUserList(SortedSet<String> userList) {
		this.userList = userList;
	}

	public String getMedia() {
		return media;
	}

	public String getRetweetsHandling() {
		return retweetsHandling;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	
	
	public Boolean isDisableTimeRange() {
		return disableTimeRange;
	}

	public void setDisableTimeRange(boolean disableTimeRange) {
		this.disableTimeRange = disableTimeRange;
	}

	/**
	 * @param overObject
	 * @return
	 * @func Overrides the equals function of the CollectRequest object. Checks that
	 *       the attributes : search and lang are the same
	 */
	@Override
	public boolean equals(Object overObject) {
		if (!(overObject instanceof CollectRequest))
			return false;
		CollectRequest overRequest = (CollectRequest) overObject;

		Boolean sameSearch = true;

		Set<String> andSet1 = this.keywordList;
		Set<String> andSet2 = overRequest.keywordList;

		Set<String> notSet1 = this.bannedWords;
		Set<String> notSet2 = overRequest.bannedWords;

		if (!equalsSet(andSet1, andSet2))
			sameSearch = false;

		if (!equalsSet(notSet1, notSet2))
			sameSearch = false;

		Boolean sameLang;
		if (this.lang != null && overRequest.lang != null)
			sameLang = this.lang.equals(overRequest.lang);
		else if (this.lang == null && overRequest.lang == null)
			sameLang = true;
		else
			sameLang = false;

		return sameSearch && sameLang;
	}

	public boolean isValid() {
		if (this.keywordList == null && userList.size() == 0)
			return false;
		if (this.keywordList != null && this.keywordList.size() == 0 && userList.size() == 0)
			return false;
		if (!this.disableTimeRange && (this.from == null || this.until == null))
			return false;
		return true;
	}

	/**
	 * @param sortedSet1
	 * @param sortedSet2
	 * @return
	 * @func Verifies if the two sets are equal
	 */
	public Boolean equalsSet(Set<String> sortedSet1, Set<String> sortedSet2) {
		if (sortedSet1 == null && sortedSet2 == null)
			return true;
		if (sortedSet1 != null && sortedSet2 != null) {
			return sortedSet1.equals(sortedSet2);
		} else
			return false;
	}
}
