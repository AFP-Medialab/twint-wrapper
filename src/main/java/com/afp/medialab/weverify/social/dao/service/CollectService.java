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
        return new CollectResponse(collectHistory.getSession(), collectHistory.getStatus(), "");
    }

    public Boolean UpdateCollectStatus(String session, Status status)
    {
        CollectHistory collectHistory = collectInterface.findCollectHistoryBySession(session);
        if (status == Status.Running && collectHistory.getStatus() == Status.Pending)
        {
            collectInterface.updateCollectProcessStart(session, new Date());
            collectInterface.updateCollectStatus(session, status);
            return true;
        }
        else if (status == Status.Done && collectHistory.getStatus() == Status.Running)
        {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, status);
            return true;
        }
        else if (status == status.Error && collectHistory.getStatus() != status.Error)
        {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, status);
            return true;
        }
        return false;
    }

    public CollectHistory getCollectInfo(String session) {
        return collectInterface.findCollectHistoryBySession(session);
    }
}
