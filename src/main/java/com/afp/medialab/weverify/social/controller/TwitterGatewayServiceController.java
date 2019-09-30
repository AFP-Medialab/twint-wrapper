package com.afp.medialab.weverify.social.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
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
import com.afp.medialab.weverify.social.twint.TwintRequestGenerator;
import com.afp.medialab.weverify.social.twint.TwintThread;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "Twitter scraping API")
public class TwitterGatewayServiceController {

    private static Logger Logger = LoggerFactory.getLogger(TwitterGatewayServiceController.class);

    @Autowired
    private CollectService collectService;

    @Autowired
    private TwintThread tt;

    @Value("${application.home.msg}")
    private String homeMsg;


    @RequestMapping(path = "/", method = RequestMethod.GET)
    public @ResponseBody
    String home() {
        return homeMsg;
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException() {
        return "This session could not be found";
    }


    @ApiOperation(value = "Trigger a Twitter Scraping")
    @RequestMapping(path = "/collect", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    CollectResponse collect(@RequestBody @Valid CollectRequest collectRequest, BindingResult result) {

        String session = UUID.randomUUID().toString();
        Logger.info(result.getAllErrors().toString());
        if (result.hasErrors()) {
            String str = "";
            for (ObjectError r : result.getAllErrors()) {
                str += r.getDefaultMessage() + "; ";
            }
            Logger.info(str);
            return new CollectResponse(session, Status.Error, str, null);
        }

        Logger.info("search : " + collectRequest.getSearch());
        Logger.info("from : " + collectRequest.getFrom().toString());
        Logger.info("until : " + collectRequest.getUntil().toString());
        Logger.info("language : " + collectRequest.getLang());
        Logger.info("user : " + collectRequest.getUser());
        Logger.info("verified : " + collectRequest.isVerified());
        Logger.info("Retweets : " + collectRequest.getRetweetsHandling());
        Logger.info("Media : " + collectRequest.getMedia());

        return caching(collectRequest, session);
    }


    @ApiOperation(value = "Trigger a status check")
    @RequestMapping(path = "/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody
    StatusResponse status(@RequestBody StatusRequest statusRequest) throws ExecutionException, InterruptedException {
        Logger.info("POST status " + statusRequest.getSession());
        return getStatusResponse(statusRequest.getSession());
    }


    @RequestMapping("/status/{id}")
    public @ResponseBody
    StatusResponse status(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        Logger.info("GET status " + id);
        return getStatusResponse(id);
    }


	/**
	 * @func 	Returns the status response of a given session.
	 * @param 	session we want the status from.
	 * @return	StatusResponse of the session.
	 * @throws 	ExecutionException
	 * @throws 	InterruptedException
	 */
    StatusResponse getStatusResponse(String session) throws ExecutionException, InterruptedException {
        CollectHistory collectHistory = collectService.getCollectInfo(session);
        if (collectHistory == null) throw new NotFoundException();

        CollectRequest collectRequest = collectService.StringToCollectRequest(collectHistory.getQuery());
        if (collectRequest != null) {
            if (collectHistory.getStatus() != Status.Done)
                return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
                        collectHistory.getStatus(), collectRequest, null, null);
            else
                return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
                        collectHistory.getStatus(), collectRequest, collectHistory.getCount(), null);
        }
        return new StatusResponse(collectHistory.getSession(), null, null, Status.Error, null, null, "No query found, or parsing error");
    }

	/**
	 * @func	Verifies if the request has already been done.
	 * 			If not creates a new session and gives a CollectResponse accordingly.
	 * @param 	newCollectRequest collect request asked.
	 * @param 	session	of the request.
	 * @return
	 */
    CollectResponse caching(CollectRequest newCollectRequest, String session) {
        // Check if this request has already been done
        CollectResponse alreadyDone = collectService.alreadyExists(newCollectRequest);
        if (alreadyDone != null) {
            Logger.info("This request has already been done sessionId: " + alreadyDone.getSession());
            return alreadyDone;
        }

        //Search for all matching queries regardless of the date
        Set<CollectHistory> collectHistories = collectService.findCollectHistoryByQueryContains(TwintRequestGenerator.generateSearch(newCollectRequest.getSearch()));
        for (CollectHistory c : collectHistories) {
            if (alreadyDone != null)
                break;
            alreadyDone = CompletingOldRequest(c.getSession(), newCollectRequest, collectService.StringToCollectRequest(c.getQuery()));
        }

        if (alreadyDone != null) {
            Logger.info("This request has already been done sessionId: " + alreadyDone.getSession());
            return alreadyDone;
        }

        // Creation of a brand new  CollectHistory
        collectService.SaveCollectInfo(session, newCollectRequest, null, null, Status.Pending, null, null);

        CompletableFuture<Map.Entry<Integer, Integer>> pair = tt.callTwint2(newCollectRequest, null, session);

        CollectHistory collectedInfo = collectService.getCollectInfo(session);


        if (collectedInfo.getStatus() != Status.Done) {
            return new CollectResponse(session, collectedInfo.getStatus(), null, collectedInfo.getProcessEnd());
        } else {
            Logger.info("PAIR : " + pair);
           /* Map.Entry<Integer, Integer> map = null;
            try {
                map = (pair.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/
        }
        return new CollectResponse(session, collectedInfo.getStatus(), collectedInfo.getMessage(), collectedInfo.getProcessEnd());
    }

	/**
	 *
	 * 	@func 	If the newCollectRequest matches the searching criteria of oldCollectRequest.
     * 				It makes a Twint call for all the newCollectRequest days except the ones
	 * 				that already have been searched by the oldCollectRequest.
	 * 			if the 	newCollectRequest does not match,
	 * 				It makes a new CollectHistory
     *
	 * @param 	session
	 * @param 	newCollectRequest
	 * @param 	oldCollectRequest
	 * @return 	corresponding collectResponse if their is a Mach, null otherwise.
	 */
	public CollectResponse CompletingOldRequest(String session, CollectRequest newCollectRequest, CollectRequest oldCollectRequest) {

        CollectRequest resultingCollectRequest = newCollectRequest;
        String message = "Completing the research. This research has already been done from " + oldCollectRequest.getFrom() + " to " + oldCollectRequest.getUntil();

        if (oldCollectRequest.equals(newCollectRequest)) {
            // The new request covers all the old request and more
            if (newCollectRequest.getFrom().compareTo(oldCollectRequest.getFrom()) < 0
                    && newCollectRequest.getUntil().compareTo(oldCollectRequest.getUntil()) > 0) {
                // Make a Twint collection from newCollectRequest.getFrom() to oldCollectRequest.getFrom()
                // And from  oldCollectRequest.getUntil() to newCollectRequest.getUntil()

                collectService.updateCollectStatus(session, Status.Pending);
                collectService.updateCollectQuery(session, resultingCollectRequest);
                CompletableFuture<Map.Entry<Integer, Integer>> DoubleFuture = callTwintOnInterval(newCollectRequest, session, newCollectRequest.getFrom(), oldCollectRequest.getFrom(), oldCollectRequest.getUntil(), newCollectRequest.getUntil());
                return getCollectResponseFromTwintCall(session, message, DoubleFuture);
            }
            // THe new request all the odl request or less
            else if (newCollectRequest.getFrom().compareTo(oldCollectRequest.getFrom()) >= 0
                    && newCollectRequest.getUntil().compareTo(oldCollectRequest.getUntil()) <= 0) {
                // Return the existing research
                String messageDone = "This research has already been done.";
                return new CollectResponse(session, collectService.getCollectInfo(session).getStatus(), messageDone, collectService.getCollectInfo(session).getProcessEnd());
            }
            // The new request covers before and a part of the old request
            else if (newCollectRequest.getFrom().compareTo(oldCollectRequest.getFrom()) < 0
                    && newCollectRequest.getUntil().compareTo(oldCollectRequest.getUntil()) <= 0
                    && newCollectRequest.getUntil().compareTo(oldCollectRequest.getFrom()) >= 0) {
                // make a search from newCollectRequest.getFrom to oldCollectRequest.getFrom()

                collectService.updateCollectStatus(session, Status.Pending);
                resultingCollectRequest.setUntil(oldCollectRequest.getUntil());
                collectService.updateCollectQuery(session, resultingCollectRequest);
                CompletableFuture<Map.Entry<Integer, Integer>> singleFuture = callTwintOnInterval(newCollectRequest, session, newCollectRequest.getFrom(), oldCollectRequest.getFrom());
                return getCollectResponseFromTwintCall(session, message, singleFuture);

            }
            // The new request covers after and a part of the old request
            else if (newCollectRequest.getUntil().compareTo(oldCollectRequest.getUntil()) > 0
                    && newCollectRequest.getFrom().compareTo(oldCollectRequest.getFrom()) >= 0
                    && newCollectRequest.getFrom().compareTo(oldCollectRequest.getUntil()) <= 0) {
                // make a search from oldCollectRequest.getUntil() to newCollectRequest.getUntil()

                collectService.updateCollectStatus(session, Status.Pending);
                resultingCollectRequest.setFrom(oldCollectRequest.getFrom());
                collectService.updateCollectQuery(session, resultingCollectRequest);
                CompletableFuture<Map.Entry<Integer, Integer>> singleFuture = callTwintOnInterval(newCollectRequest, session, oldCollectRequest.getUntil(), newCollectRequest.getUntil());
                return getCollectResponseFromTwintCall(session, message, singleFuture);
            }
            // else {
            // The new request covers no days in common with the old one
            // Undefined case yet so it's ignored for now.
            // This collect request will have a new session Id even thought it matched the research criteria.
            // }
            return null;
        }
        return null;
    }


	/**
	 * @func 	getCollectResponseFromTwintCall
	 * @param 	session	of the collectHistory
	 * @param 	message
	 * @param 	pair
	 * @return	the collect response corresponding to the new call to Twint.
	 */
    public CollectResponse getCollectResponseFromTwintCall(String session, String message, CompletableFuture<Map.Entry<Integer, Integer>> pair) {
        CollectHistory collectedInfo = collectService.getCollectInfo(session);
        if (collectedInfo.getStatus() != Status.Done)
            return new CollectResponse(session, collectedInfo.getStatus(), message, collectedInfo.getProcessEnd());
        /*try {
            Map.Entry<Integer, Integer> map = pair.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/
        return new CollectResponse(session, collectService.getCollectInfo(session).getStatus(), message, collectService.getCollectInfo(session).getProcessEnd());
    }

	/**
	 * @func	callTwintOnInterval overload with only one date interval.
	 * 			call Twint on the interval and append the result to the elastic search session
	 * @param 	newCollectRequest
	 * @param 	session
	 * @param 	from
	 * @param 	until
	 * @return
	 */
    public CompletableFuture<Map.Entry<Integer, Integer>> callTwintOnInterval(CollectRequest newCollectRequest, String session, Date from, Date until) {
        return callTwintOnInterval(newCollectRequest, session, from, until, null, null);
    }

	/**
	 * @func	callTwintOnInterval overload with only two date interval.
	 * 			call Twint one time on each intervals and append the result to the elastic search session
	 * @param 	newCollectRequest
	 * @param 	session
	 * @param 	from1
	 * @param 	until1
	 * @param 	from2
	 * @param 	until2
	 * @return
	 */
    public CompletableFuture<Map.Entry<Integer, Integer>> callTwintOnInterval(CollectRequest newCollectRequest, String session, Date from1, Date until1, Date from2, Date until2) {
        // Update the session with the good dates;
        CollectRequest collectRequest1 = new CollectRequest(newCollectRequest);
        collectRequest1.setFrom(from1);
        collectRequest1.setUntil(until1);
        CollectRequest collectRequest2 = null;
        if (from2 != null && until2 != null) {
            collectRequest2 = new CollectRequest(newCollectRequest);
            collectRequest2.setFrom(from2);
            collectRequest2.setUntil(until2);
        }
        return tt.callTwint2(collectRequest1, collectRequest2, session);
    }


    @RequestMapping(value = "/collect-history", method = RequestMethod.GET)
    public @ResponseBody
    HistoryResponse collectHistory(@RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
                                   @RequestParam(value = "asc", required = false, defaultValue = "false") boolean asc,
                                   @RequestParam(value = "desc", required = false, defaultValue = "false") boolean desc,
                                   @RequestParam(value = "status", required = false) String status) {
        List<CollectHistory> last;

        if (!asc && !desc)
            last = collectService.getLasts(limit);
        else if (status == null)
            last = collectService.getAll(desc);
        else {
            Logger.info("STATUS : " + status);
            last = collectService.getByStatus(status);
        }
        return new HistoryResponse(last);
    }

    @ApiOperation(value = "Get the requests history")
    @RequestMapping(path = "/collect-history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody
    HistoryResponse status(@RequestBody @Valid HistoryRequest historyRequest) {
        Logger.info("POST collect-history : " + historyRequest.toString());
        List<CollectHistory> collectHistoryList = collectService.getHistory(historyRequest.getLimit(), historyRequest.getStatus(),
                (historyRequest.getSort() == null ? false : historyRequest.getSort().equals("desc")),
                historyRequest.getProcessStart(), historyRequest.getProcessTo());
        return new HistoryResponse(collectHistoryList);
    }

    @RequestMapping("/collect-update/{id}")
    public @ResponseBody
    StatusResponse collectUpdate(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return collectUpdateFunction(id);
    }

    @ApiOperation(value = "Update an old request")
    @RequestMapping(path = "/collect-update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody
    StatusResponse collectUpdate(@RequestBody @Valid CollectUpdateRequest collectUpdateRequest) throws ExecutionException, InterruptedException {
        String id = collectUpdateRequest.getSession();
        return collectUpdateFunction(id);
    }

	/**
	 * @func 	Calls Twint on the time interval of a session. The result replaces the old ones un elastic search.
	 * @param 	session
	 * @return	Status response or the corresponding session
	 * @throws 	ExecutionException
	 * @throws 	InterruptedException
	 */
    public StatusResponse collectUpdateFunction(String session) throws ExecutionException, InterruptedException {
        Logger.info("Collect-Update " + session);

        CollectHistory collectHistory = collectService.getCollectInfo(session);
        if (collectHistory == null) throw new NotFoundException();


        if (collectHistory.getStatus() != Status.Done) {
            StatusResponse res = getStatusResponse(session);
            res.setMessage("This session is already updating");
            return res;
        }

        collectService.updateCollectStatus(session, Status.Pending);
        CollectRequest oldCollectRequest = collectService.StringToCollectRequest(collectService.getCollectInfo(session).getQuery());
        if (oldCollectRequest == null) throw new NotFoundException();

        //CompletableFuture<Map.Entry<Integer, Integer>> singleFuture = callTwintOnInterval(oldCollectRequest, session, oldCollectRequest.getFrom(), oldCollectRequest.getUntil());
        callTwintOnInterval(oldCollectRequest, session, oldCollectRequest.getFrom(), oldCollectRequest.getUntil());
        collectService.updateCollectProcessStart(session, new Date());
        return getStatusResponse(session);
    }
}