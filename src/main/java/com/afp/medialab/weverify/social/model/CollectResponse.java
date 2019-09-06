package com.afp.medialab.weverify.social.model;

public class CollectResponse {

	private String session;
	private Status status;

	public CollectResponse(String session, Status status) {
		this.session = session;
		this.status = status;
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

}
