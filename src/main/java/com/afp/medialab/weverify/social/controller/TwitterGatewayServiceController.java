package com.afp.medialab.weverify.social.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.afp.medialab.weverify.social.twint.TwintThread;
import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.validation.Valid;

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

    private CompletableFuture<Integer> nb_tweet;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public @ResponseBody
    String home() {
        return homeMsg;
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


    StatusResponse getStatusResponse(String session) throws ExecutionException, InterruptedException {
        CollectHistory collectHistory = collectService.getCollectInfo(session);
        if (collectHistory == null)
            return new StatusResponse(session, null, null, Status.Error, null, null);

        CollectRequest collectRequest = collectService.StringToCollectRequest(collectHistory.getQuery());
        if (collectRequest != null) {
			if (collectHistory.getStatus() != Status.Done)
				return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
						collectHistory.getStatus(), collectRequest, null);
			else
            	return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
                    collectHistory.getStatus(), collectRequest, nb_tweet.get());
        }
        return new StatusResponse(collectHistory.getSession(), null, null, Status.Error, null, null);
    }

    CollectResponse caching(CollectRequest newCollectRequest, String session) {
        // Check if this request has already been donne
        CollectResponse alreadyDonne = collectService.alreadyExists(newCollectRequest);
		if (alreadyDonne != null) {
			Logger.info("This request has already been donne sessionId: " + alreadyDonne.getSession());
			return alreadyDonne;
		}

		//Search for all matching queries regardless of the date
		Set<CollectHistory> collectHistories = collectService.findCollectHistoryByQueryContains(newCollectRequest.getSearch());
		for (CollectHistory c : collectHistories) {
			if (alreadyDonne != null)
				break;
			alreadyDonne = CompletingOldRequest(c.getSession(), newCollectRequest, collectService.StringToCollectRequest(c.getQuery()));
		}

		if (alreadyDonne != null) {
			Logger.info("This request has already been donne sessionId: " + alreadyDonne.getSession());
			return alreadyDonne;
		}

        // Creation of a brand new  CollectHistory
        collectService.SaveCollectInfo(session, newCollectRequest, null, null, Status.Pending);

        nb_tweet = tt.callTwint(newCollectRequest, session);

        CollectHistory CollectedInfo = collectService.getCollectInfo(session);

        return new CollectResponse(session, CollectedInfo.getStatus(), CollectedInfo.getMessage(), CollectedInfo.getProcessEnd());
    }

    /*
    * CompletingOldRequest :
    *
    * 	If the newCollectRequest matches the searching criteria of oldCollectRequest.
    * 	It makes a Twint call for all the newCollectRequest days except the ones that already have been searched by the oldCollectRequest.
    *
    * 	Returns The corresponding collectResponse if their is a Mach, null otherwise.
    *
    * */
	public CollectResponse CompletingOldRequest(String session, CollectRequest newCollectRequest, CollectRequest oldCollectRequest){

		CollectRequest resultingCollectRequest = newCollectRequest;
		String message = "Completing the research. This research has already been donne from " + oldCollectRequest.getFrom() + " to " + oldCollectRequest.getUntil();

		if (oldCollectRequest.equals(newCollectRequest)) {
			// The new request covers all the old request and more
			if (newCollectRequest.getFrom().compareTo(oldCollectRequest.getFrom()) < 0
			 && newCollectRequest.getUntil().compareTo(oldCollectRequest.getUntil()) > 0) {
				// Make a Twint collection from newCollectRequest.getFrom() to oldCollectRequest.getFrom()
				// And from  oldCollectRequest.getUntil() to newCollectRequest.getUntil()

				collectService.updateCollectStatus(session, Status.Pending);
				collectService.updateCollectQuery(session, resultingCollectRequest);

				callTwintOnInterval(newCollectRequest, session, newCollectRequest.getFrom(), oldCollectRequest.getFrom());
				collectService.updateCollectStatus(session, Status.Pending);
				callTwintOnInterval(newCollectRequest, session, oldCollectRequest.getUntil(), newCollectRequest.getUntil());

				return new CollectResponse(session, collectService.getCollectInfo(session).getStatus(),  message, collectService.getCollectInfo(session).getProcessEnd());
			}
			// THe new request all the odl request or less
			else if (newCollectRequest.getFrom().compareTo(oldCollectRequest.getFrom()) >= 0
					&& newCollectRequest.getUntil().compareTo(oldCollectRequest.getUntil()) <= 0) {
				// Return the existing research
				String messageDone = "This research has already been donne.";
				return new CollectResponse(session, collectService.getCollectInfo(session).getStatus(),  messageDone, collectService.getCollectInfo(session).getProcessEnd());
			}
			// The new request covers before and a part of the old request
			else if (newCollectRequest.getFrom().compareTo(oldCollectRequest.getFrom()) < 0
					&& newCollectRequest.getUntil().compareTo(oldCollectRequest.getUntil()) <= 0
					&& newCollectRequest.getUntil().compareTo(oldCollectRequest.getFrom()) >= 0) {
				// make a search from newCollectRequest.getFrom to oldCollectRequest.getFrom()

				collectService.updateCollectStatus(session, Status.Pending);
				resultingCollectRequest.setUntil(oldCollectRequest.getUntil());
				collectService.updateCollectQuery(session, resultingCollectRequest);
				callTwintOnInterval(newCollectRequest, session, newCollectRequest.getFrom(), oldCollectRequest.getFrom());
				return new CollectResponse(session, collectService.getCollectInfo(session).getStatus(),  message, collectService.getCollectInfo(session).getProcessEnd());

			}
			// The new request covers after and a part of the old request
			else if (newCollectRequest.getUntil().compareTo(oldCollectRequest.getUntil()) > 0
					&& newCollectRequest.getFrom().compareTo(oldCollectRequest.getFrom()) >= 0
					&& newCollectRequest.getFrom().compareTo(oldCollectRequest.getUntil()) <= 0) {
				// make a search from oldCollectRequest.getUntil() to newCollectRequest.getUntil()

				collectService.updateCollectStatus(session, Status.Pending);
				resultingCollectRequest.setFrom(oldCollectRequest.getFrom());
				collectService.updateCollectQuery(session, resultingCollectRequest);
				callTwintOnInterval(newCollectRequest, session, oldCollectRequest.getUntil(), newCollectRequest.getUntil());
				return new CollectResponse(session, collectService.getCollectInfo(session).getStatus(),  message, collectService.getCollectInfo(session).getProcessEnd());
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

	public void callTwintOnInterval(CollectRequest newCollectRequest, String session, Date from, Date until){
		// Update the session with the good dates;
		CollectRequest collectRequest = newCollectRequest;
		collectRequest.setFrom(from);
		collectRequest.setUntil(until);
		nb_tweet = tt.callTwint(collectRequest, session);
	}
}