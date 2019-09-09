package com.afp.medialab.weverify.social.controller;

import java.util.Date;
import java.util.UUID;

import com.afp.medialab.weverify.social.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TwitterGatewayServiceController {
	
	private static Logger Logger = LoggerFactory.getLogger(TwitterGatewayServiceController.class);
	
	@Value("${application.home.msg}")
	private String homeMsg;
	
	
	@RequestMapping("/")
	public @ResponseBody String home() {
		return homeMsg;
	}

	@RequestMapping(path = "/collect", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody CollectResponse collect(@RequestBody CollectRequest collectRequest) {
		Logger.info(collectRequest.getSearch());
		Logger.info(collectRequest.getFrom().toString());
		Logger.info(collectRequest.getUntil().toString());
		String session = UUID.randomUUID().toString();
		return new CollectResponse(session, Status.Done);

	}

	@RequestMapping(path = "/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody StatusResponse statusResponse(@RequestBody StatusRequest statusRequest) {
		Logger.info(statusRequest.getSearch());
		String session = UUID.randomUUID().toString();
		return new StatusResponse(session, new Date(), new Date(), Status.Done, new CollectRequest(session, new Date(), new Date()));

	}
}
