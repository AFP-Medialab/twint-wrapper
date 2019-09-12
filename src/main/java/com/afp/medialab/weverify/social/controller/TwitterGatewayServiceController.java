package com.afp.medialab.weverify.social.controller;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.afp.medialab.weverify.social.TwintCall;
import com.afp.medialab.weverify.social.TwintThread;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
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
	public @ResponseBody String home() {
		return homeMsg;
	}


	@ApiOperation(value = "Trigger a Twitter Scraping")
	@RequestMapping(path = "/collect", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody CollectResponse collect(@RequestBody CollectRequest collectRequest) {
		Logger.info(collectRequest.getSearch());
		if (collectRequest.getFrom() != null)
			Logger.info(collectRequest.getFrom().toString());

		if (collectRequest.getFrom() != null)
			Logger.info(collectRequest.getUntil().toString());
		String session = UUID.randomUUID().toString();

		collectService.SaveCollectInfo(session, collectRequest, null, null, Status.Pending);

		tt.callTwint(collectRequest, session);

		Logger.info(collectService.getCollectInfo(session).getStatus().toString());
		return new CollectResponse(session, collectService.getCollectInfo(session).getStatus());
	}

	@ApiOperation(value = "Trigger a status check")
	@RequestMapping(path = "/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody StatusResponse status(@RequestBody StatusRequest statusRequest) {
		Logger.info(statusRequest.getSession());

		CollectHistory collectHistory = collectService.getCollectInfo(statusRequest.getSession());
		if (collectHistory == null)
			return new StatusResponse(statusRequest.getSession(), null, null, Status.Error, null);

		ObjectMapper mapper = new ObjectMapper();
		try {
			CollectRequest collectRequest = mapper.readValue(collectHistory.getQuery(), CollectRequest.class);
			return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(), collectHistory.getProcessEnd(),
					collectHistory.getStatus(), collectRequest);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new StatusResponse(collectHistory.getSession(), null, null, Status.Error, null);
	}
}
