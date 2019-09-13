package com.afp.medialab.weverify.social.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class StatusResponse {

    private String session;
    private Date started, ended;
    private Status status;
    private CollectRequest query;
    private String nbTweets;

    public StatusResponse(String session, Date started, Date ended, Status status, CollectRequest query, Integer nbTweet) {
        this.session = session;
        this.started = started;
        this.ended = ended;
        this.status = status;
        this.query = query;
        if (nbTweet != null)
            this.nbTweets = Integer.toString(nbTweet);
        else
            this.nbTweets = null;

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

    public String getNbTweets() {
        return nbTweets;
    }

    public void setNbTweets(String nbTweets) {
        this.nbTweets = nbTweets;
    }
}
