package com.afp.medialab.weverify.social.dao.service;

import java.util.ArrayList;
import java.util.Date;

import com.afp.medialab.weverify.social.twint.TwintThreadExecutor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
@Service
public class CollectService {

    private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TwintThreadExecutor.class);
    @Autowired
    CollectInterface collectInterface;


    public String CollectRequestToString(CollectRequest collectRequest){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(collectRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "Json parsing Error";
    }

    public void SaveCollectInfo(String session, CollectRequest collectRequest, Date processStart, Date processEnd, Status status)
    {
        CollectHistory collectHistory = new CollectHistory(session, CollectRequestToString(collectRequest), processStart, processEnd, status);
        collectInterface.save(collectHistory);
    }

    public CollectResponse alreadyExists(CollectRequest collectRequest){
        CollectHistory collectHistory = collectInterface.findCollectHistoryByQuery(CollectRequestToString(collectRequest));
        if (collectHistory == null)
            return null;
        return new CollectResponse(collectHistory.getSession(), collectHistory.getStatus(), null, collectHistory.getProcessEnd());
    }

    public Boolean UpdateCollectStatus(String session, Status newstatus)
    {
        Status existingStatus = collectInterface.findCollectHistoryBySession(session).getStatus();
        if (newstatus == Status.Running && existingStatus == Status.Pending)
        {
            collectInterface.updateCollectProcessStart(session, new Date());
            collectInterface.updateCollectStatus(session, newstatus.toString());
            return true;
        }
        else if (newstatus == Status.Done && existingStatus == Status.Running)
        {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, newstatus.toString());
            return true;
        }
        else if (newstatus == Status.Error && existingStatus != Status.Error)
        {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, newstatus.toString());
            return true;
        }
        return false;
    }

    public CollectHistory getCollectInfo(String session) {
        return collectInterface.findCollectHistoryBySession(session);
    }

    public List<CollectHistory> getLasts(int nb)
    {
        List<CollectHistory> collectHistoryList = collectInterface.findAll();
        Collections.reverse(collectHistoryList);

        if (collectHistoryList.size() >= nb)
           collectHistoryList = collectHistoryList.subList(0, nb);
       return collectHistoryList;
    }

    public List<CollectHistory> getAll(boolean desc)
    {
        List<CollectHistory> collectHistoryList = collectInterface.findAll();
        if (desc)
            Collections.reverse(collectHistoryList);

        return collectHistoryList;
    }

    public List<CollectHistory> getByStatus(String status)
    {
        List<CollectHistory> collectHistoryList = collectInterface.findCollectHistoryByStatus(status);
        return collectHistoryList;
    }

    public  List<CollectHistory> getHistory(int limit, String status, boolean desc, Date processStart, Date processEnd) {
        List<CollectHistory> collectHistoryList = null;
        if (status != null && processEnd != null && processStart != null)
           collectHistoryList = collectInterface.findCollectHistoryByProcessEndLessThanEqualOrProcessEndIsNullAndProcessStartGreaterThanEqualAndStatus(processEnd, processStart, status);
        else if (status != null && processEnd == null && processStart == null)
                collectHistoryList = collectInterface.findCollectHistoryByStatus(status);
        else if (status != null && processEnd != null)
            collectHistoryList = collectInterface.findCollectHistoryByStatusAndProcessEndLessThan(status, processEnd);
        else if (status != null)
            collectHistoryList = collectInterface.findCollectHistoryByStatusAndProcessStartGreaterThan(status, processStart);
        else if (processEnd != null)
            collectHistoryList = collectInterface.findCollectHistoryByProcessEndLessThan(processEnd);
        else if (processStart != null)
            collectHistoryList = collectInterface.findCollectHistoryByProcessStartGreaterThan(processStart);
        else {

            collectHistoryList = collectInterface.findAll();

        }
        if (desc)
            Collections.reverse(collectHistoryList);

        if (limit != 0 && collectHistoryList.size() > limit)
            collectHistoryList = collectHistoryList.subList(0, limit);
        return collectHistoryList;
    }

    public void UpdateCollectMessage(String session, String message)
    {
        collectInterface.updateCollectMessage(session, message);
    }
}

