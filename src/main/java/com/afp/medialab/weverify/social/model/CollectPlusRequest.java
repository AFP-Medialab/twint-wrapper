package com.afp.medialab.weverify.social.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.afp.medialab.weverify.social.constrains.LangConstrain;
import com.afp.medialab.weverify.social.constrains.MediaConstrain;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Model for Tplus
 * @author Bertrand Goupil
 *
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CollectPlusRequest {

	@NotNull(message = "keywords are mandatory")
	private String keywords;
	private String bannedWords, usernames;
	private String filter;
	@JsonDeserialize(using = MultiDateDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date since;
	@JsonDeserialize(using = MultiDateDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date from;

	@LangConstrain
	private String lang;
	@MediaConstrain
	private String media;

	private Boolean verified = false;

	private Boolean disableTimeRange = false;

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getBannedWords() {
		return bannedWords;
	}

	public void setBannedWords(String bannedWords) {
		this.bannedWords = bannedWords;
	}

	public String getUsernames() {
		return usernames;
	}

	public void setUsernames(String usernames) {
		this.usernames = usernames;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Date getSince() {
		return since;
	}

	public void setSince(Date since) {
		this.since = since;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public Boolean getVerified() {
		return verified;
	}

	public void setVerified(Boolean verified) {
		this.verified = verified;
	}

	public Boolean getDisableTimeRange() {
		return disableTimeRange;
	}

	public void setDisableTimeRange(Boolean disableTimeRange) {
		this.disableTimeRange = disableTimeRange;
	}

}
