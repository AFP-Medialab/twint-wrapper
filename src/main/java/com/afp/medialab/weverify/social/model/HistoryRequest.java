package com.afp.medialab.weverify.social.model;

import com.afp.medialab.weverify.social.constrains.SortConstrain;
import com.afp.medialab.weverify.social.constrains.StatusConstrain;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class HistoryRequest {

    private int limit;
    @SortConstrain
    private String sort;
    @StatusConstrain
    private String status;
    private Date processFrom;
    private Date processTo;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getProcessFrom() {
        return processFrom;
    }

    public void setProcessFrom(Date processFrom) {
        this.processFrom = processFrom;
    }

    public Date getProcessTo() {
        return processTo;
    }

    public void setProcessTo(Date processTo) {
        this.processTo = processTo;
    }
}
