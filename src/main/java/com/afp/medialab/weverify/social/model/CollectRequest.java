package com.afp.medialab.weverify.social.model;

//import com.afp.medialab.weverify.social.exceptions.BadRequestException;
import com.afp.medialab.weverify.social.constrains.MediaConstrain;
import com.afp.medialab.weverify.social.constrains.RetweetHandlingConstrain;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class CollectRequest {

	private String search;
	private String lang;
	private String user;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date from, until;

	@MediaConstrain
	private String media;

	private Boolean verified = false;

	@RetweetHandlingConstrain
	private String retweetsHandling;



	public CollectRequest(){}


	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
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

}
