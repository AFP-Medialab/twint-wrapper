package com.afp.medialab.weverify.social.controller;

import java.util.Date;
import java.util.UUID;

import com.afp.medialab.weverify.social.TwintCall;
import com.afp.medialab.weverify.social.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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
		Logger.info(collectRequest.getFrom().toString());
		Logger.info(collectRequest.getUntil().toString());
		String session = UUID.randomUUID().toString();
		TwintCall tc = new TwintCall(collectRequest, session);
		tc.collect();
		return new CollectResponse(session, Status.Done);
	}

	@ApiOperation(value = "Trigger a status check")
	@RequestMapping(path = "/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody StatusResponse status(@RequestBody StatusRequest statusRequest) {
		Logger.info(statusRequest.getSession());
		String session = UUID.randomUUID().toString();
		return new StatusResponse(session, new Date(), new Date(), Status.Done, new CollectRequest(session, new Date(), new Date()));
	}
}
