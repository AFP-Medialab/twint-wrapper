package com.afp.medialab.weverify.social.controller;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.entity.Request;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.CollectUpdateRequest;
import com.afp.medialab.weverify.social.model.HistoryRequest;
import com.afp.medialab.weverify.social.model.HistoryResponse;
import com.afp.medialab.weverify.social.model.NotFoundException;
import com.afp.medialab.weverify.social.model.Status;
import com.afp.medialab.weverify.social.model.StatusRequest;
import com.afp.medialab.weverify.social.model.StatusResponse;
import com.afp.medialab.weverify.social.twint.TwintThreadGroup;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "Twitter scraping API")
public class TwitterGatewayServiceController {

    private static Logger Logger = LoggerFactory.getLogger(TwitterGatewayServiceController.class);

    @Autowired
    private CollectService collectService;

    @Autowired
    private TwintThreadGroup ttg;

    @Value("${application.home.msg}")
    private String homeMsg;


    @RequestMapping(path = "/", method = RequestMethod.GET)
    public @ResponseBody
    String home() {
        return homeMsg;
    }


    @ApiOperation(value = "Trigger a Twitter Scraping")
    @RequestMapping(path = "/collect", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    CollectResponse collect(@RequestBody @Valid CollectRequest collectRequest, BindingResult result) {

        if (!collectRequest.isValid())
            return new CollectResponse(null, Status.Error, "and_list or user_list must be given, from and until are mandatory", null);

        Logger.debug(result.getAllErrors().toString());
        if (result.hasErrors()) {
            String str = "";
            for (ObjectError r : result.getAllErrors()) {
                str += r.getDefaultMessage() + "; ";
            }
            Logger.info(str);
            return new CollectResponse(null, Status.Error, str, null);
        }

        Set<String> and_list = collectRequest.getKeywordList();
        Set<String> not_ist = collectRequest.getBannedWords();
        if (and_list != null)
            Logger.debug("and_list : " + and_list.toString());
        if (not_ist != null)
            Logger.debug("not_list : " + not_ist.toString());
        Logger.debug("from : " + collectRequest.getFrom().toString());
        Logger.debug("until : " + collectRequest.getUntil().toString());
        Logger.debug("language : " + collectRequest.getLang());
        Logger.debug("user : " + collectRequest.getUserList());
        Logger.debug("verified : " + collectRequest.isVerified());
        Logger.debug("Retweets : " + collectRequest.getRetweetsHandling());
        Logger.debug("Media : " + collectRequest.getMedia());

        return useCache(collectRequest);
    }


    @ApiOperation(value = "Trigger a status check")
    @RequestMapping(path = "/status", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    StatusResponse status(@RequestBody StatusRequest statusRequest) {
        Logger.debug("POST status " + statusRequest.getSession());
        return getStatusResponse(statusRequest.getSession());
    }


    @ApiOperation(value = "Trigger a status check")
    @RequestMapping(path = "/status/{id}", method = RequestMethod.GET)
    public @ResponseBody
    StatusResponse status(@PathVariable("id") String id) {
        Logger.debug("GET status " + id);
        return getStatusResponse(id);
    }


    /**
     * @param session we want the status from.
     * @return StatusResponse of the session.
     * @func Returns the status response of a given session.
     */
    private StatusResponse getStatusResponse(String session) {
        CollectHistory collectHistory = collectService.getCollectInfo(session);
        if (collectHistory == null) throw new NotFoundException();

        CollectRequest collectRequest = new CollectRequest(collectHistory.getRequest());
        if (collectHistory.getStatus() != Status.Done)
            return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
                    collectHistory.getStatus(), collectRequest, null, null);
        else
            return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
                    collectHistory.getStatus(), collectRequest, collectHistory.getCount(), collectHistory.getMessage());
    }


    private Set<Request> requestIsInCache(CollectRequest collectRequest) {
        Set<Request> keywords = collectService.requestsContainingOnlySomeOfTheKeywords(collectRequest.getKeywordList(), collectRequest.getLang());
        Set<Request> banned_words = collectService.requestContainingOnlySomeOfTheBannedWords(collectRequest.getBannedWords());
        Set<Request> users = collectService.requestContainingAllTheUsers(collectRequest.getUserList());

        Set<Request> maching_requests = new HashSet<Request>();
        if (keywords != null && keywords.size() > 0) {
            maching_requests.addAll(keywords);
            if (banned_words != null)
                maching_requests.retainAll(banned_words);
            else
                maching_requests.retainAll(collectService.requestContainingEmptyBannedWords());
            if (users != null)
                maching_requests.retainAll(users);
            else
                maching_requests.retainAll(collectService.requestContainingEmptyUsers());
        } else {
            maching_requests = users;
        }
        return maching_requests;
    }

    private Set<Request> similarInCache(CollectRequest collectRequest) {
        Set<Request> keywords = collectService.requestsContainingAllTheKeywords(collectRequest.getKeywordList(), collectRequest.getLang());
        Set<Request> bannedWords = collectService.requestContainingAllTheBannedWords(collectRequest.getBannedWords());
        Set<Request> users = collectService.requestsContainingOnlySomeOfTheUsers(collectRequest.getUserList());

        Set<Request> maching_requests;
        if (keywords != null) {
            maching_requests = new HashSet<Request>(keywords);
            if (bannedWords != null)
                maching_requests.retainAll(bannedWords);
            if (users != null)
                maching_requests.retainAll(users);
        } else {
            maching_requests = users;
        }
        return maching_requests;
    }

    private CollectResponse makeRequestFromListAndCompleteTime(CollectRequest collectRequest, Set<Request> requests) {
    	if(requests == null) return null; 
        for (Request request : requests) {
            CollectResponse result = makeRequestIfDatesAreLarger(collectRequest, request);
            if (result != null)
                return result;
        }
        return null;
    }

    private CollectResponse makeLargerRequestFromListAndCompleteTime(CollectRequest collectRequest, Set<Request> requests) {
        for (Request request : requests) {
            CollectResponse result = makeLargerRequestIfDatesAreLarger(collectRequest, request);
            if (result != null)
                return result;
        }
        return null;
    }

    private CollectResponse makeLargerRequestIfDatesAreLarger(CollectRequest newCollectRequest, Request oldRequest) {
        CollectHistory collectHistory = collectService.findCollectHistoryByRequest(oldRequest);
        collectHistory.setMessage("Completing the research done from " + oldRequest.getSince() + " to " + oldRequest.getUntil());


        // The new request covers all the old request and more
        if (newCollectRequest.getFrom().compareTo(oldRequest.getSince()) < 0
                && newCollectRequest.getUntil().compareTo(oldRequest.getUntil()) > 0) {
            // Make a Twint collection from newCollectRequest.getFrom() to newCollectRequest.getUntil()
            // And from  oldRequest.getUntil() to newCollectRequest.getUntil()

            collectHistory.setStatus(Status.Pending);
            oldRequest.update(newCollectRequest);
            collectService.save_request(oldRequest);
            collectService.save_collectHistory(collectHistory);

            ttg.callTwintMultiThreaded(collectHistory, newCollectRequest);
            return getCollectResponseFromTwintCall(collectHistory);
        }
        // The new request covers all the old request or less
        else if (newCollectRequest.getFrom().compareTo(oldRequest.getSince()) >= 0
                && newCollectRequest.getUntil().compareTo(oldRequest.getUntil()) <= 0) {
            // Research the old request
            collectHistory.setMessage("Completing the research");
            collectHistory.setStatus(Status.Pending);
            newCollectRequest.setFrom(oldRequest.getSince());
            newCollectRequest.setUntil(oldRequest.getUntil());
            oldRequest.update(newCollectRequest);
            collectService.save_request(oldRequest);
            collectService.save_collectHistory(collectHistory);

            ttg.callTwintMultiThreaded(collectHistory, newCollectRequest);
            return new CollectResponse(collectHistory);
        }
        // The new request covers before and a part of the old request
        else if (newCollectRequest.getFrom().compareTo(oldRequest.getSince()) < 0
                && newCollectRequest.getUntil().compareTo(oldRequest.getUntil()) <= 0
                && newCollectRequest.getUntil().compareTo(oldRequest.getSince()) >= 0) {
            // make a search from newCollectRequest.getFrom to oldRequest.getUntil()
            collectHistory.setStatus(Status.Pending);
            newCollectRequest.setUntil(oldRequest.getUntil());
            oldRequest.update(newCollectRequest);
            collectService.save_request(oldRequest);
            collectService.save_collectHistory(collectHistory);

            ttg.callTwintMultiThreaded(collectHistory, newCollectRequest);
            return new CollectResponse(collectHistory);

        }
        // The new request covers after and a part of the old request
        else if (newCollectRequest.getUntil().compareTo(oldRequest.getUntil()) > 0
                && newCollectRequest.getFrom().compareTo(oldRequest.getSince()) >= 0
                && newCollectRequest.getFrom().compareTo(oldRequest.getUntil()) <= 0) {
            // make a search from oldRequest.getFrom() to newCollectRequest.getUntil()

            collectHistory.setStatus(Status.Pending);
            newCollectRequest.setFrom(oldRequest.getSince());
            oldRequest.update(newCollectRequest);
            collectService.save_request(oldRequest);
            collectService.save_collectHistory(collectHistory);

            ttg.callTwintMultiThreaded(collectHistory, newCollectRequest);
            return new CollectResponse(collectHistory);
        }
        // else {
        // The new request covers no days in common with the old one
        // Undefined case yet so it's ignored for now.
        // This collect request will have a new session Id even thought it matched the research criteria.
        // }
        return null;
    }

    /**
     * @param collectRequest collect request asked.
     * @return
     * @func Verifies if the request has already been done.
     * If not creates a new session and gives a CollectResponse accordingly.
     */
    private CollectResponse useCache(CollectRequest collectRequest) {
        // Find previous request that are larger than @collectRequest.
        Set<Request> previousMatch = requestIsInCache(collectRequest);
        CollectResponse alreadyDone = makeRequestFromListAndCompleteTime(collectRequest, previousMatch);
        if (alreadyDone != null) {
            Logger.info("This request is contained in an already done request,  sessionId: " + alreadyDone.getSession());
            return alreadyDone;
        }
        // Find previous request that are smaller than @collectRequest but similar.
        previousMatch = similarInCache(collectRequest);
        CollectResponse completing = makeLargerRequestFromListAndCompleteTime(collectRequest, previousMatch);
        if (completing != null) {
            Logger.info("This request extends an already done request,  sessionId: " + completing.getSession());
            return completing;
        }

        // Creation of a brand new  CollectHistory
        String session = UUID.randomUUID().toString();
        Logger.debug(session);
        Logger.debug(collectRequest.toString());
        Date now = Calendar.getInstance().getTime();
        CollectHistory collectHistory = collectService.saveCollectInfo(session, collectRequest, now , null, Status.Pending, null, null, 0, 0, 0);
        ttg.callTwintMultiThreaded(collectHistory, collectRequest);

        if (collectHistory.getStatus() != Status.Done)
            return new CollectResponse(session, collectHistory.getStatus(), null, collectHistory.getProcessEnd());
        return new CollectResponse(session, collectHistory.getStatus(), collectHistory.getMessage(), collectHistory.getProcessEnd());
    }

    /**
     * @param newCollectRequest
     * @param oldCollectRequest
     * @return corresponding collectResponse if their is a Mach, null otherwise.
     * @func If the newCollectRequest matches the searching criteria of oldCollectRequest.
     * It makes a Twint call for all the newCollectRequest days except the ones
     * that already have been searched by the oldCollectRequest.
     * if the 	newCollectRequest does not match,
     * It makes a new CollectHistory
     */
    private CollectResponse makeRequestIfDatesAreLarger(CollectRequest newCollectRequest, Request oldCollectRequest) {
    	//TODO custom message when dates are out of range e.g previous 15h->19h  new 20h->21h get the max range => 15h to 21h
        CollectHistory collectHistory = collectService.findCollectHistoryByRequest(oldCollectRequest);
        collectHistory.setMessage("Completing the research. This research has already been done from " + oldCollectRequest.getSince() + " to " + oldCollectRequest.getUntil());

        Date oldFrom = oldCollectRequest.getSince();
        Date oldUntil = oldCollectRequest.getUntil();
        Date newFrom = newCollectRequest.getFrom();
        Date newUntil = newCollectRequest.getUntil();
        
        // The new request covers all the old request and more before or after
        if (newFrom.compareTo(oldFrom) < 0
                || newUntil.compareTo(oldUntil) > 0) {
            // Make a Twint collection from newCollectRequest.getFrom() to oldCollectRequest.getFrom()
            // And from  oldCollectRequest.getUntil() to newCollectRequest.getUntil()

            collectHistory.setStatus(Status.Pending);
            collectHistory.setProcessStart(Calendar.getInstance().getTime());
            collectHistory.setProcessEnd(null);
            collectService.save_collectHistory(collectHistory);
            
            if(newFrom.compareTo(oldFrom) < 0) {
            	// Search date is before previous from range
            	oldCollectRequest.setSince(newCollectRequest.getFrom());
            	callTwintOnInterval(collectHistory, oldCollectRequest, newFrom, oldFrom);
            }
            if(newUntil.compareTo(oldUntil) > 0) {
            	// Search date is after previous from range
            	oldCollectRequest.setUntil(newCollectRequest.getUntil());
            	callTwintOnInterval(collectHistory, oldCollectRequest, oldUntil, newUntil);
            }
            collectService.save_request(oldCollectRequest);
            return getCollectResponseFromTwintCall(collectHistory);
        }
        // new request all the old request or less
        else {
            // Return the existing research
            collectHistory.setMessage("This research has already been done.");
            return new CollectResponse(collectHistory);
        }
       
    }

    /**
     * @return the collect response corresponding to the new call to Twint.
     * @func getCollectResponseFromTwintCall
     */
    private CollectResponse getCollectResponseFromTwintCall(CollectHistory collectHistory) {
        if (collectHistory.getStatus() != Status.Done)
            return new CollectResponse(collectHistory);
        return new CollectResponse(collectHistory);
    }

    /**
     * @param from
     * @param until
     * @return
     * @func callTwintOnInterval overload with only one date interval.
     * call Twint on the interval and append the result to the elastic search session
     */
    private void callTwintOnInterval(CollectHistory collectHistory, Request request, Date from, Date until) {
        CollectRequest newCollectRequest = new CollectRequest(request);
        newCollectRequest.setFrom(from);
        newCollectRequest.setUntil(until);
        ttg.callTwintMultiThreaded(collectHistory, newCollectRequest);
    }


    @RequestMapping(value = "/collect-history", method = RequestMethod.GET)
    public @ResponseBody
    HistoryResponse collectHistory(@RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
                                   @RequestParam(value = "asc", required = false, defaultValue = "false") boolean asc,
                                   @RequestParam(value = "desc", required = false, defaultValue = "false") boolean desc,
                                   @RequestParam(value = "status", required = false) String status) {
        List<CollectHistory> last;

        Logger.info("GET collect-history :  " + status);

        if (status == null) {
            if (!asc && !desc)
                last = collectService.getLasts(limit, true);
            else if (asc)
                last = collectService.getLasts(limit, !asc);
            else
                last = collectService.getLasts(limit, desc);
        } else {
            if (!asc && !desc)
                last = collectService.getByStatus(status, limit, true);
            else if (asc)
                last = collectService.getByStatus(status, limit, !asc);
            else
                last = collectService.getByStatus(status, limit, desc);


        }
        return new HistoryResponse(last);
    }

    @ApiOperation(value = "Get the requests history")
    @RequestMapping(path = "/collect-history", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    HistoryResponse status(@RequestBody @Valid HistoryRequest historyRequest) {
        Logger.info("POST collect-history : " + historyRequest.toString());
        List<CollectHistory> collectHistoryList = collectService.getHistory(historyRequest.getLimit(), historyRequest.getStatus(),
                (historyRequest.getSort() == null ? false : historyRequest.getSort().equals("desc")),
                historyRequest.getProcessStart(), historyRequest.getProcessTo());
        return new HistoryResponse(collectHistoryList);
    }

    @ApiOperation(value = "Update an old request")
    @RequestMapping(path = "/collect-update/{id}", method = RequestMethod.GET)
    public @ResponseBody
    StatusResponse collectUpdate(@PathVariable("id") String id) throws ExecutionException, InterruptedException, IOException {
        return collectUpdateFunction(id);
    }

    @ApiOperation(value = "Update an old request")
    @RequestMapping(path = "/collect-update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    StatusResponse collectUpdate(@RequestBody @Valid CollectUpdateRequest collectUpdateRequest) throws ExecutionException, InterruptedException, IOException {
        String id = collectUpdateRequest.getSession();
        return collectUpdateFunction(id);
    }

    /**
     * @param session
     * @return Status response or the corresponding session
     * @throws ExecutionException
     * @throws InterruptedException
     * @func Calls Twint on the time interval of a session. The result replaces the old ones un elastic search.
     */
    private StatusResponse collectUpdateFunction(String session) throws ExecutionException, InterruptedException {
        Logger.info("Collect-Update " + session);

        CollectHistory collectHistory = collectService.getCollectInfo(session);
        if (collectHistory == null) throw new NotFoundException();


        if (collectHistory.getStatus() != Status.Done) {
            StatusResponse res = getStatusResponse(session);
            res.setMessage("This session is already updating");
            return res;
        }

        collectHistory.setStatus(Status.Pending);
        Request request = collectHistory.getRequest();
        if (request == null) {
            collectHistory.setStatus(Status.Error);
            collectHistory.setMessage("Could not find the Request associated");
            collectService.save_collectHistory(collectHistory);
            throw new NotFoundException();
        }
        collectHistory.setProcessStart(new Date());
        collectService.save_collectHistory(collectHistory);
        callTwintOnInterval(collectHistory, request, request.getSince(), request.getUntil());
        return getStatusResponse(session);
    }
}