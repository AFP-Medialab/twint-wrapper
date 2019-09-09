package com.afp.medialab.weverify.social.model;

import java.util.Date;

public class CollectRequest {

	private String search;
	private Date from, until;

	public CollectRequest(String search, Date from, Date until) {
		this.search = search;
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

}
