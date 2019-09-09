package com.afp.medialab.weverify.social.model;

import java.util.Date;

public class StatusResponse {

    private String session;
    private Date started, ended;
    private Status status;
    private CollectRequest query;

    public StatusResponse(String session, Date started, Date ended, Status status, CollectRequest query) {
        this.session = session;
        this.started = started;
        this.ended = ended;
        this.status = status;
        this.query = query;
    }

    public String getSession() { return session; }

    public void setSession(String search) {
        this.session = search;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getEnded() {
        return ended;
    }

    public void setEnded(Date until) {
        this.ended = ended;
    }

    public CollectRequest getQuery() { return this.query; }

    public Status getStatus() { return this.status; }

    public void SetStatus(Status status) { this.status = status; }

    public void SetQuery(CollectRequest query) { this.query = query; }

}
