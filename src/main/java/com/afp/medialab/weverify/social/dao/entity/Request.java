package com.afp.medialab.weverify.social.dao.entity;

import com.afp.medialab.weverify.social.model.CollectRequest;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.SortedSet;

@Entity
public class Request implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "keyWords", nullable = true, updatable = true)
    @ElementCollection(targetClass=String.class)
    @OrderBy("sort ASC")
    private SortedSet<String> keywords;

    @Column(name = "bannedWords", nullable = true, updatable = true)
    @ElementCollection(targetClass=String.class)
    @OrderBy("sort ASC")
    private SortedSet<String> bannedWords;

    @Column(name = "language")
    private String language;

    @Column(name = "user_list", nullable = true, updatable = true)
    @ElementCollection(targetClass=String.class)
    @OrderBy("sort ASC")
    private SortedSet<String> user_list;

    @Column(name = "since")
    private Date since;

    @Column(name = "until")
    private Date until;

    @Column(name = "media_type")
    private String media;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "retweetsHandling")
    private String retweetsHandling;

    public Request(){}

    public Request(SortedSet<String> keywords, SortedSet<String> bannedWords, String language, SortedSet<String> user_list, Date since, Date until, String media) {
        this.keywords = keywords;
        this.bannedWords = bannedWords;
        this.language = language;
        this.user_list = user_list;
        this.since = since;
        this.until = until;
        this.media = media;
    }

    public Request(CollectRequest collectRequest){
        this.keywords = collectRequest.getAnd_list();
        this.bannedWords = collectRequest.getNot_list();
        this.language = collectRequest.getLang();
        this.user_list = collectRequest.getUser_list();
        this.since = collectRequest.getFrom();
        this.until = collectRequest.getUntil();
        this.media = collectRequest.getMedia();
    }

    public void update(CollectRequest collectRequest){
        this.keywords = collectRequest.getAnd_list();
        this.bannedWords = collectRequest.getNot_list();
        this.language = collectRequest.getLang();
        this.user_list = collectRequest.getUser_list();
        this.since = collectRequest.getFrom();
        this.until = collectRequest.getUntil();
        this.media = collectRequest.getMedia();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SortedSet<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(SortedSet<String> keywords) {
        this.keywords = keywords;
    }

    public SortedSet<String> getBannedWords() {
        return bannedWords;
    }

    public void setBannedWords(SortedSet<String> bannedWords) {
        this.bannedWords = bannedWords;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public SortedSet<String> getUser_list() {
        return user_list;
    }

    public void setUser_list(SortedSet<String> user_list) {
        this.user_list = user_list;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getRetweetsHandling() {
        return retweetsHandling;
    }

    public void setRetweetsHandling(String retweetsHandling) {
        this.retweetsHandling = retweetsHandling;
    }
}
