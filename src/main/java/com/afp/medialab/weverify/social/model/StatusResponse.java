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
    private Integer finished_threads;
    private Integer total_threads;
    private Integer successful_threads;

    public StatusResponse(String session, Date started, Date ended, Status status, CollectRequest query, Integer nbTweet, String message, Integer finished_threads, Integer total_threads, Integer successful_threads) {
        this.session = session;
        this.started = started;
        this.ended = ended;
        this.status = status;
        this.query = query;
        this.message = message;
        this.nbTweets = nbTweet;
        this.finished_threads = finished_threads;
        this.total_threads = total_threads;
        this.successful_threads = successful_threads;
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

    public Integer getFinished_threads() {
        return finished_threads;
    }

    public void setFinished_threads(Integer finished_threads) {
        this.finished_threads = finished_threads;
    }

    public Integer getTotal_threads() {
        return total_threads;
    }

    public void setTotal_threads(Integer total_threads) {
        this.total_threads = total_threads;
    }

    public Integer getSuccessful_threads() {
        return successful_threads;
    }

    public void setSuccessful_threads(Integer successful_threads) {
        this.successful_threads = successful_threads;
    }
}
