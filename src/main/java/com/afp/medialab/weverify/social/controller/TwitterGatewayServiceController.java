package com.afp.medialab.weverify.social.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

	private Integer nb_tweet;




	@RequestMapping(path = "/", method = RequestMethod.GET)
	public @ResponseBody String home() {
		return homeMsg;
	}




	@ApiOperation(value = "Trigger a Twitter Scraping")
	@RequestMapping(path = "/collect", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody CollectResponse collect(@RequestBody @Valid CollectRequest collectRequest, BindingResult result) throws ExecutionException, InterruptedException {

		String session = UUID.randomUUID().toString();
		Logger.info(result.getAllErrors().toString());
		if (result.hasErrors())
		{
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

		// Check if this request has already been donne, if it does return it
		CollectResponse alreadyDonne = collectService.alreadyExists(collectRequest);
		if (alreadyDonne != null) {
			Logger.info("This request has already been donne sessionId: " + alreadyDonne.getSession());
			return alreadyDonne;
		}


		collectService.SaveCollectInfo(session, collectRequest, null, null, Status.Pending);

		CompletableFuture<Map.Entry<Integer, Integer>> pair = tt.callTwint2(collectRequest, null, session);

		CollectHistory collectedInfo = collectService.getCollectInfo(session);

		if (collectedInfo.getStatus() != Status.Done)
		{
			return new CollectResponse(session, collectedInfo.getStatus(),null, collectedInfo.getProcessEnd());
		}
		else {
			Logger.info("PAIR : " + pair);
			Map.Entry<Integer, Integer> map = (pair.get());
			nb_tweet = map.getKey();
		}
		return new CollectResponse(session, collectedInfo.getStatus(),collectedInfo.getMessage(), collectedInfo.getProcessEnd());
	}

	@ApiOperation(value = "Trigger a status check")
	@RequestMapping(path = "/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody StatusResponse status(@RequestBody StatusRequest statusRequest) {
		Logger.info("POST status " + statusRequest.getSession());
        return getStatusResponse(statusRequest.getSession());
	}


    @RequestMapping("/status/{id}")
    public @ResponseBody StatusResponse status(@PathVariable("id") String id) {
        Logger.info("GET status " + id);
        return getStatusResponse(id);
    }


    StatusResponse getStatusResponse(String session){
        CollectHistory collectHistory = collectService.getCollectInfo(session);
        if (collectHistory == null)
            return new StatusResponse(session, null, null, Status.Error, null, null);

        ObjectMapper mapper = new ObjectMapper();
        try {
            CollectRequest collectRequest = mapper.readValue(collectHistory.getQuery(), CollectRequest.class);
			if (collectHistory.getStatus() != Status.Done)
            	return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
                    collectHistory.getStatus(), collectRequest, null);
			else
				return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
						collectHistory.getStatus(), collectRequest, nb_tweet);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
		}
        return new StatusResponse(collectHistory.getSession(), null, null, Status.Error, null, null);
    }



	@RequestMapping(value = "/collect-history", method = RequestMethod.GET)
	public @ResponseBody HistoryResponse collectHistory(@RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
														@RequestParam(value = "asc", required = false, defaultValue = "false") boolean asc,
														@RequestParam(value = "desc", required = false, defaultValue = "false") boolean desc,
														@RequestParam(value = "status", required = false) String status)
	{
		List<CollectHistory> last = null;

		if (!asc && !desc)
			last = collectService.getLasts(limit);
		else if (status == null)
			last = collectService.getAll(desc);
		else
		{
			Logger.info("STATUS : " + status);
			last = collectService.getByStatus(status);
		}
		return new HistoryResponse(last);
	}

	@ApiOperation(value = "Get the requests history")
	@RequestMapping(path = "/collect-history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody HistoryResponse status(@RequestBody @Valid HistoryRequest historyRequest) {
		Logger.info("POST collect-history : " + historyRequest.toString());
		List<CollectHistory> collectHistoryList = collectService.getHistory(historyRequest.getLimit(), historyRequest.getStatus(),
																			(historyRequest.getSort() == null?false : historyRequest.getSort().equals("desc")),
																			historyRequest.getProcessStart(), historyRequest.getProcessTo());
		return new HistoryResponse(collectHistoryList);
	}




}