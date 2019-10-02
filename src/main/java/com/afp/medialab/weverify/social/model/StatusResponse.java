package com.afp.medialab.weverify.social.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResponse {

    private String session;
    private Date started, ended;
    private Status status;
    private CollectRequest query;
    private Integer nbTweets;
    private String message;

    public StatusResponse(String session, Date started, Date ended, Status status, CollectRequest query, Integer nbTweet, String message) {
        this.session = session;
        this.started = started;
        this.ended = ended;
        this.status = status;
        this.query = query;
        this.message = message;
        this.nbTweets = nbTweet;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSession() {
        return session;
    }

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
        this.ended = until;
    }

    public CollectRequest getQuery() {
        return this.query;
    }

    public Status getStatus() {
        return this.status;
    }

    public void SetStatus(Status status) {
        this.status = status;
    }

    public void SetQuery(CollectRequest query) {
        this.query = query;
    }

    public Integer getNbTweets() {
        return nbTweets;
    }

    public void setNbTweets(Integer nbTweets) {
        this.nbTweets = nbTweets;
    }
}
