package com.afp.medialab.weverify.social.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)

public class CollectResponse {

	private String session;
	private Status status;
	private String message;
	private Date lastSearch;

	public CollectResponse(String session, Status status, String message, Date lastSearch) {
		this.session = session;
		this.status = status;
		this.message = message;

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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	public Date getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(Date lastSearch) {
		this.lastSearch = lastSearch;
	}
}
