package com.afp.medialab.weverify.social.model;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class HistoryResponse {
    private List<CollectHistory> requests;

    public HistoryResponse(List<CollectHistory> requests)
    {
        this.requests = requests;
    }

    public List<CollectHistory> getRequests() {
        return requests;
    }

    public void setRequests(List<CollectHistory> requests) {
        this.requests = requests;
    }
}
