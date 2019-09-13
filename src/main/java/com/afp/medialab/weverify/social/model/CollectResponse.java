package com.afp.medialab.weverify.social.model;

import java.util.Date;

public class CollectResponse {

	private String session;
	private Status status;
	private Date lastSearch;

	public CollectResponse(String session, Status status, Date lastSearch) {
		this.session = session;
		this.status = status;
		this.lastSearch = lastSearch;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(Date lastSearch) {
		this.lastSearch = lastSearch;
	}
}
