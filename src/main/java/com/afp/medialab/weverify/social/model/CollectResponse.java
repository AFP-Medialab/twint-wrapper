package com.afp.medialab.weverify.social.model;

public class CollectResponse {

	private String session;
	private Status status;
	private String message;

	public CollectResponse(String session, Status status, String message) {
		this.session = session;
		this.status = status;
		this.message = message;
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
}
