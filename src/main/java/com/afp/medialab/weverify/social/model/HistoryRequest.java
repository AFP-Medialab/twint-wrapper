package com.afp.medialab.weverify.social.model;

import com.afp.medialab.weverify.social.constrains.SortConstrain;
import com.afp.medialab.weverify.social.constrains.StatusConstrain;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryRequest {

    private int limit;
    @SortConstrain
    private String sort;
    @StatusConstrain
    private String status;
    private Date processStart;
    private Date processEnd;

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

    public Date getProcessStart() {
        return processStart;
    }

    public void setProcessStart(Date processStart) {
        this.processStart = processStart;
    }

    public Date getProcessTo() {
        return processEnd;
    }

    public void setProcessTo(Date processTo) {
        this.processEnd = processTo;
    }

    public String toString()
    {
        return "History request : \n    " + "Limit: " + limit + "\n   " + "Sort: " + sort + "\n    " + "Status: " +
                status + "\n    ProcessStart: " + processStart + "\n    ProcessEnd: " + processEnd;

    }
}
