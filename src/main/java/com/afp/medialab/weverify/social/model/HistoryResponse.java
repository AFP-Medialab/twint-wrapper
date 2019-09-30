package com.afp.medialab.weverify.social.model;

import java.util.List;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
