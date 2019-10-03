package com.afp.medialab.weverify.social.model;

import java.util.Date;
import java.util.List;

import com.afp.medialab.weverify.social.constrains.LangConstrain;
import com.afp.medialab.weverify.social.constrains.MediaConstrain;
import com.afp.medialab.weverify.social.constrains.RetweetHandlingConstrain;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectRequest {

    private SearchModel search;

    @LangConstrain
    private String lang;
    private List<String> user_list;
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
        this.search = collectRequest.search;
        this.lang = collectRequest.lang;
        this.user_list = collectRequest.user_list;
        this.from = collectRequest.from;
        this.until = collectRequest.until;
        this.media = collectRequest.media;
        this.verified = collectRequest.verified;
        this.retweetsHandling = collectRequest.retweetsHandling;
    }

    public SearchModel getSearch() {
        return search;
    }

    public void setSearch(SearchModel search) {
        this.search = search;
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

    public List<String> getUser_list() {
        return user_list;
    }

    public void setUser_list(List<String> user_list) {
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

        Boolean sameSearch;
        if (this.search != null && overRequest.search != null)
            sameSearch = this.search.equals(overRequest.search);
        else if (this.search == null && overRequest.search == null)
            sameSearch = true;
        else
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
}
