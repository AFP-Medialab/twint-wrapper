package com.afp.medialab.weverify.social.dao.service;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.afp.medialab.weverify.social.model.StatusResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CollectService {

    //private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TwintThreadExecutor.class);
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

    public CollectRequest StringToCollectRequest(String query){
        ObjectMapper mapper = new ObjectMapper();
        try {
            CollectRequest collectRequest = mapper.readValue(query, CollectRequest.class);
            return collectRequest;
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public Boolean updateCollectStatus(String session, Status newStatus)
    {
        CollectHistory collectHistory = collectInterface.findCollectHistoryBySession(session);
        Status existingStatus = collectHistory.getStatus();
        if (newStatus == Status.Pending && existingStatus == Status.Done)
        {
            collectInterface.updateCollectProcessEnd(session, null);
            collectInterface.updateCollectStatus(session, newStatus.toString());
            return true;
        }
        if (newStatus == Status.Running && existingStatus == Status.Pending)
        {
            if (collectHistory.getProcessStart() == null)
                collectInterface.updateCollectProcessStart(session, new Date());
            collectInterface.updateCollectStatus(session, newStatus.toString());
            return true;
        }
        else if (newStatus == Status.Done && existingStatus == Status.Running)
        {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, newStatus.toString());
            return true;
        }
        else if (newStatus == Status.Error && existingStatus != Status.Error)
        {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, newStatus.toString());
            return true;
        }
        return false;
    }

    public void updateCollectQuery(String session, CollectRequest collectRequest)
    {
        String query = CollectRequestToString(collectRequest);
        collectInterface.updateCollectQuery(session, query);
    }

    public void updateCollectProcessEnd(String session, Date date)
    {
        collectInterface.updateCollectProcessEnd(session, date);
    }

    public void updateCollectProcessStart(String session, Date date)
    {
        collectInterface.updateCollectProcessStart(session, date);
    }

    public CollectHistory getCollectInfo(String session) {
        return collectInterface.findCollectHistoryBySession(session);
    }

    public void updateCollectMessage(String session, String message)
    {
        collectInterface.updateCollectMessage(session, message);
    }

    public Set<CollectHistory> findCollectHistoryByQueryContains(String str){
        return collectInterface.findCollectHistoryByQueryContains(str);
    }
}
