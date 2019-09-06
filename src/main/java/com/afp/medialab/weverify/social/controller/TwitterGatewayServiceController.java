package com.afp.medialab.weverify.social.controller;

import java.util.UUID;

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
	
	@RequestMapping("/")
	public @ResponseBody String home() {
		return "service is up";
	}

	@RequestMapping(path = "/collect", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody CollectResponse collect(@RequestBody CollectRequest collectRequest) {
		String session = UUID.randomUUID().toString();
		return new CollectResponse(session, Status.Done);

	}
}
