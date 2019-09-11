package com.afp.medialab.weverify.social.dao.service;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class CollectService {

    @Autowired
    CollectInterface collectInterface;

    public void SaveCollectInfo(String session, CollectRequest collectRequest, Date processStart, Date processEnd, Status status)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        String query = "{\n" +
                            "\"search\" : \"" + collectRequest.getSearch() + "\",\n" +
                            "\"from\" : \"" + dateFormat.format(collectRequest.getFrom())+ "\",\n" +
                            "\"until\" : \"" + dateFormat.format(collectRequest.getUntil()) + "\"\n" +
                        "}";
        CollectHistory collectHistory = new CollectHistory(session, query, processStart, processEnd, status);
        collectInterface.save(collectHistory);
    }

    public Boolean UpdateCollectStatus(String session, Status status)
    {
        CollectHistory collectHistory = collectInterface.findCollectHistoryBySession(session);
        if (status == Status.Running && collectHistory.getStatus() == Status.NotStarted)
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
