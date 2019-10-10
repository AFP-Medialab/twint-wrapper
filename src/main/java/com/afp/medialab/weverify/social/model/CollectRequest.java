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


    private SortedSet<String> and_list;
    private SortedSet<String> not_list;

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
        this.and_list = collectRequest.and_list;
        this.not_list = collectRequest.not_list;
        this.lang = collectRequest.lang;
        this.user_list = collectRequest.user_list;
        this.from = collectRequest.from;
        this.until = collectRequest.until;
        this.media = collectRequest.media;
        this.verified = collectRequest.verified;
        this.retweetsHandling = collectRequest.retweetsHandling;
    }
    public CollectRequest(Request request) {
        this.and_list = request.getKeywords();
        this.not_list = request.getBannedWords();
        this.lang = request.getLanguage();
        this.user_list = request.getUser_list();
        this.from = request.getSince();
        this.until = request.getUntil();
        this.media = request.getMedia();
        this.verified = request.getVerified();
        this.retweetsHandling = request.getRetweetsHandling();
    }

    public SortedSet<String> getAnd_list() {
        return and_list;
    }

    public void setAnd_list(SortedSet<String> and_list) {
        this.and_list = and_list;
    }

    public SortedSet<String> getNot_list() {
        return not_list;
    }

    public void setNot_list(SortedSet<String> not_list) {
        this.not_list = not_list;
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

        SortedSet<String> andSet1 = this.and_list;
        SortedSet<String> andSet2 = overRequest.and_list;

        SortedSet<String> notSet1 = this.not_list;
        SortedSet<String> notSet2 = overRequest.not_list;

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
        if (this.and_list == null  && user_list.size() == 0)
            return false;
        if (this.and_list != null && this.and_list.size() == 0 && user_list.size() == 0)
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
