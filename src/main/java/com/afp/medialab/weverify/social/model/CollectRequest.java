package com.afp.medialab.weverify.social.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.logging.Logger;

import com.afp.medialab.weverify.social.constrains.LangConstrain;
import com.afp.medialab.weverify.social.constrains.MediaConstrain;
import com.afp.medialab.weverify.social.constrains.RetweetHandlingConstrain;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectRequest {

    private SearchModel search;

    @LangConstrain
    private String lang;
    private String user;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date from, until;

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
        this.user = collectRequest.user;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    //public String toString()
    //{
    //}

    public Boolean equalsSet(SortedSet sortedSet1, SortedSet sortedSet2)
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

    @Override
    public boolean equals(Object overObject) {
        if (!(overObject instanceof CollectRequest))
            return false;

        CollectRequest overRequest = (CollectRequest) overObject;

        Boolean sameSearch = true;
        if (this.search != null && overRequest.search != null)
        {
            String search1 = this.search.getSearch();
            String search2 = overRequest.search.getSearch();

            SortedSet andSet1 = this.search.getAnd();
            SortedSet andSet2 = overRequest.search.getAnd();

            SortedSet orSet1 = this.search.getOr();
            SortedSet orSet2 = overRequest.search.getOr();

            SortedSet notSet1 = this.search.getNot();
            SortedSet notSet2 = overRequest.search.getNot();

            if (search1 != null && search2 != null)
                sameSearch = search1.equals(search2);
            else if (!(search1 == null && search2 == null))
                sameSearch = false;

            if (!equalsSet(andSet1, andSet2))
                sameSearch = false;

            if (!equalsSet(orSet1, orSet2))
                sameSearch = false;

            if (!equalsSet(notSet1, notSet2))
                sameSearch = false;
        }
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
