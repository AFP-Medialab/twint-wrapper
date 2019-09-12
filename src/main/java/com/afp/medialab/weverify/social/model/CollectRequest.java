package com.afp.medialab.weverify.social.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;

public class CollectRequest {

	private String search;
	private String lang;
	private String user;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date from, until;

	public CollectRequest(){}

	public CollectRequest(String search, String lang, String user, Date from, Date until) {
		this.search = search;
		this.lang = lang;
		this.user = user;
		this.from = from;
		this.until = until;
	}

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
}
