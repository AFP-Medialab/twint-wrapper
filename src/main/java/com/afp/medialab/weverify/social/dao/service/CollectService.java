package com.afp.medialab.weverify.social.dao.service;

import com.afp.medialab.weverify.social.twint.TwintThreadExecutor;
import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

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

    public Boolean UpdateCollectStatus(String session, Status Newstatus)
    {
        Status existingStatus = collectInterface.findCollectHistoryBySession(session).getStatus();
        if (Newstatus == Status.Running && existingStatus == Status.Pending)
        {
            collectInterface.updateCollectProcessStart(session, new Date());
            collectInterface.updateCollectStatus(session, Newstatus.toString());
            return true;
        }
        else if (Newstatus == Status.Done && existingStatus == Status.Running)
        {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, Newstatus.toString());
            return true;
        }
        else if (Newstatus == Newstatus.Error && existingStatus != Newstatus.Error)
        {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, Newstatus.toString());
            return true;
        }
        return false;
    }

    public CollectHistory getCollectInfo(String session) {
        return collectInterface.findCollectHistoryBySession(session);
    }

    public void UpdateCollectMessage(String session, String message)
    {
        collectInterface.updateCollectMessage(session, message);
    }
}
