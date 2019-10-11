package com.afp.medialab.weverify.social.dao.service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.afp.medialab.weverify.social.dao.entity.Request;
import com.afp.medialab.weverify.social.dao.repository.RequestInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CollectService {

    //private static org.slf4j.Logger Logger = LoggerFactory.getLogger(CollectService.class);
    @Autowired
    CollectInterface collectInterface;

    @Autowired
    RequestInterface requestInterface;


    private String collectRequestToString(CollectRequest collectRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(collectRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "Json parsing Error";
    }

    public CollectRequest stringToCollectRequest(String query) {
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

    public CollectHistory saveCollectInfo(String session, CollectRequest collectRequest, Date processStart, Date processEnd, Status status, String message, Integer count, Integer finished_threads, Integer total_threads, Integer successful_threads) {
        CollectHistory collectHistory = new CollectHistory(session, new Request(collectRequest), processStart, processEnd, status, message, count, finished_threads, total_threads, successful_threads);
        return collectInterface.save(collectHistory);
    }

    public CollectResponse alreadyExists(CollectRequest collectRequest) {
        Request request = null;
        CollectHistory collectHistory = collectInterface.findCollectHistoryByRequest(request);
        if (collectHistory != null)
            return new CollectResponse(collectHistory.getSession(), collectHistory.getStatus(), null, collectHistory.getProcessEnd());
        return null;

    }

    public Boolean updateCollectStatus(String session, Status newStatus) {
        CollectHistory collectHistory = collectInterface.findCollectHistoryBySession(session);
        Status existingStatus = collectHistory.getStatus();
        if (newStatus == Status.Pending && existingStatus == Status.Done) {
            collectInterface.updateCollectProcessEnd(session, null);
            collectInterface.updateCollectStatus(session, newStatus.toString());
            if (collectHistory.getTotal_threads() == null) {
                collectInterface.updateCollectTotal_threads(session, 0);
                collectInterface.updateCollectFinished_threads(session, 0);
                collectInterface.updateCollectSuccessful_threads(session, 0);
            }
            return true;
        }
        if (newStatus == Status.Running && existingStatus == Status.Pending) {
            if (collectHistory.getProcessStart() == null)
                collectInterface.updateCollectProcessStart(session, new Date());
            collectInterface.updateCollectStatus(session, newStatus.toString());
            return true;
        } else if (newStatus == Status.Done && existingStatus == Status.Running) {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, newStatus.toString());
            return true;
        } else if (newStatus == Status.Error && existingStatus != Status.Error) {
            collectInterface.updateCollectProcessEnd(session, new Date());
            collectInterface.updateCollectStatus(session, newStatus.toString());
            return true;
        }
        return false;
    }

    public void updateCollectQuery(String session, CollectRequest collectRequest) {
        CollectHistory collectHistory = collectInterface.findCollectHistoryBySession(session);
        Request request = collectHistory.getRequest();
        request.update(collectRequest);
        requestInterface.save(request);
    }

    public void updateCollectProcessEnd(String session, Date date) {
        collectInterface.updateCollectProcessEnd(session, date);
    }

    public void updateCollectProcessStart(String session, Date date) {
        collectInterface.updateCollectProcessStart(session, date);
    }

    public CollectHistory getCollectInfo(String session) {
        return collectInterface.findCollectHistoryBySession(session);
    }

    public List<CollectHistory> getLasts(int nb, boolean desc) {
        List<CollectHistory> collectHistoryList = collectInterface.findAll();
        if (desc)
            Collections.reverse(collectHistoryList);

        if (collectHistoryList.size() >= nb)
            collectHistoryList = collectHistoryList.subList(0, nb);
        return collectHistoryList;
    }

    public List<CollectHistory> getAll(boolean desc) {
        List<CollectHistory> collectHistoryList = collectInterface.findAll();
        if (desc)
            Collections.reverse(collectHistoryList);

        return collectHistoryList;
    }

    public List<CollectHistory> getByStatus(String status, int limit,  boolean desc) {
        List<CollectHistory> collectHistoryList = collectInterface.findCollectHistoryByStatus(status);
        if (desc)
            Collections.reverse(collectHistoryList);

        if (collectHistoryList.size() >= limit)
            collectHistoryList = collectHistoryList.subList(0, limit);
        return collectHistoryList;
    }

    public List<CollectHistory> getHistory(int limit, String status, boolean desc, Date processStart, Date processEnd) {
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

    public void updateCollectMessage(String session, String message) {
        collectInterface.updateCollectMessage(session, message);
    }

    public void updateCollectCount(String session, Integer count) {
        collectInterface.updateCollectCount(session, count);
    }

    public void updateCollectFinishedThreads(String session, Integer finished_threads) {
        collectInterface.updateCollectFinished_threads(session, finished_threads);
    }

    public void updateCollectTotalThreads(String session, Integer total_threads) {
        collectInterface.updateCollectTotal_threads(session, total_threads);
    }

    public void updateCollectSuccessfulThreads(String session, Integer sucessful_threads) {
        collectInterface.updateCollectSuccessful_threads(session, sucessful_threads);
    }

    public Set<Request> isContainedKeywords(CollectRequest collectRequest){
        Set<Request> matching_keyWords = new HashSet<Request>();
        Set<String> keywords = collectRequest.getKeywordList();
        if (keywords == null)
            return null;
        for (String keyword : keywords) {
            List<Request> collected = requestInterface.my_findMatchingRequestByKeyword(keyword, keywords.size());
            matching_keyWords.addAll(collected);
        }

        return matching_keyWords.stream().filter(e -> e.getKeywordList().stream().anyMatch(keywords::contains)).collect(Collectors.toSet());
    }

    public Set<Request> isContainedBannedWords(CollectRequest collectRequest){
        Set<Request> matching_bannedWords = new HashSet<Request>();
        Set<String> bannedWords = collectRequest.getKeywordList();
        if (bannedWords == null)
            return null;
        for (String bannedWord : bannedWords) {
            List<Request> collected = requestInterface.my_findMatchingRequestByBannedWords(bannedWord, bannedWords.size());
            matching_bannedWords.addAll(collected);
        }
        return matching_bannedWords.stream().filter(e -> e.getBannedWords().stream().anyMatch(bannedWords::contains)).collect(Collectors.toSet());
    }

    public Set<Request> isContainedUsers(CollectRequest collectRequest) {
        Set<Request> matching_users = new HashSet<Request>();
        Set<String> users = collectRequest.getUser_list();
        if (users == null)
            return new HashSet<>();
        for (String user : users) {
            List<Request> collected = requestInterface.my_findMatchingRequestByUsers(user, users.size());
            matching_users.addAll(collected);
        }
        return matching_users.stream().filter(e -> e.getBannedWords().contains(users) || e.getUser_list().size() == 0).collect(Collectors.toSet());
    }

    public CollectHistory findCollectHistoryByRequest(Request request)
    {
        return collectInterface.findCollectHistoryByRequest(request);
    }

    public void save_collectHistory(CollectHistory collectHistory){
        collectInterface.save(collectHistory);
    }

    public void save_request(Request request){
        requestInterface.save(request);
    }
}

