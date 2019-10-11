package com.afp.medialab.weverify.social.model;

import java.util.Date;
import java.util.SortedSet;

import com.afp.medialab.weverify.social.constrains.LangConstrain;
import com.afp.medialab.weverify.social.constrains.MediaConstrain;
import com.afp.medialab.weverify.social.constrains.RetweetHandlingConstrain;
import com.afp.medialab.weverify.social.dao.entity.Request;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CollectRequest {


    private SortedSet<String> keywords;
    private SortedSet<String> bannedWords;

    @LangConstrain
    private String lang;
    private SortedSet<String> user_list;
    @JsonProperty("from")@JsonDeserialize(using =  MultiDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date from;
    @JsonProperty("until")@JsonDeserialize(using =  MultiDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date until;

    @MediaConstrain
    private String media;

    private Boolean verified = false;

    @RetweetHandlingConstrain
    private String retweetsHandling;

    public CollectRequest() {
    }

    public CollectRequest(CollectRequest collectRequest) {
        this.keywords = collectRequest.keywords;
        this.bannedWords = collectRequest.bannedWords;
        this.lang = collectRequest.lang;
        this.user_list = collectRequest.user_list;
        this.from = collectRequest.from;
        this.until = collectRequest.until;
        this.media = collectRequest.media;
        this.verified = collectRequest.verified;
        this.retweetsHandling = collectRequest.retweetsHandling;
    }
    public CollectRequest(Request request) {
        this.keywords = request.getKeywords();
        this.bannedWords = request.getBannedWords();
        this.lang = request.getLanguage();
        this.user_list = request.getUser_list();
        this.from = request.getSince();
        this.until = request.getUntil();
        this.media = request.getMedia();
        this.verified = request.getVerified();
        this.retweetsHandling = request.getRetweetsHandling();
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

    public String getLang() {
        return lang;
    }

    public SortedSet<String> getUser_list() {
        return user_list;
    }

    public void setUser_list(SortedSet<String> user_list) {
        this.user_list = user_list;
    }

    public String getMedia() {
        return media;
    }

    public String getRetweetsHandling() {
        return retweetsHandling;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * @func    Overrides the equals function of the CollectRequest object.
     *          Checks that the attributes : search and lang are the same
     * @param   overObject
     * @return
     */
    @Override
    public boolean equals(Object overObject) {
        if (!(overObject instanceof CollectRequest))
            return false;
        CollectRequest overRequest = (CollectRequest) overObject;

        Boolean sameSearch = true;

        SortedSet<String> andSet1 = this.keywords;
        SortedSet<String> andSet2 = overRequest.keywords;

        SortedSet<String> notSet1 = this.bannedWords;
        SortedSet<String> notSet2 = overRequest.bannedWords;

        if (!equalsSet(andSet1, andSet2))
            sameSearch = false;

        if (!equalsSet(notSet1, notSet2))
            sameSearch = false;

        Boolean sameLang;
        if (this.lang != null && overRequest.lang != null)
            sameLang = this.lang.equals(overRequest.lang);
        else if (this.lang == null && overRequest.lang == null)
            sameLang = true;
        else
            sameLang = false;

        return sameSearch && sameLang;
    }

    public  boolean isValid(){
        if (this.keywords == null  && user_list.size() == 0)
            return false;
        if (this.keywords != null && this.keywords.size() == 0 && user_list.size() == 0)
            return false;
        if (this.from == null || this.until == null)
            return false;
        return true;
    }

    /**
     * @func    Verifies if the two sets are equal
     * @param   sortedSet1
     * @param   sortedSet2
     * @return
     */
    public Boolean equalsSet(SortedSet<String> sortedSet1, SortedSet<String> sortedSet2)
    {
        if (sortedSet1 == null && sortedSet2 == null)
            return true;
        if (sortedSet1 != null && sortedSet2 != null)
        {
            return sortedSet1.equals(sortedSet2);
        }
        else
            return false;
    }
}
