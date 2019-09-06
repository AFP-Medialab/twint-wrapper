package com.afp.medialab.weverify.social.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;

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
}
