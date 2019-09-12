package com.afp.medialab.weverify.social.controller;

import java.io.IOException;
import java.util.UUID;

import com.afp.medialab.weverify.social.TwintThread;
import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.service.CollectService;
//import com.afp.medialab.weverify.social.exceptions.BadRequestException;
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

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public @ResponseBody String home() {
		return homeMsg;
	}


	@ApiOperation(value = "Trigger a Twitter Scraping")
	@RequestMapping(path = "/collect", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody CollectResponse collect(@RequestBody @Valid CollectRequest collectRequest, BindingResult result) {

		String session = UUID.randomUUID().toString();
		Logger.info(result.getAllErrors().toString());
		if (result.hasErrors())
		{
			String str = "";
			for (ObjectError r : result.getAllErrors()) {
				str += r.getDefaultMessage() + "; ";
			}

			Logger.info(str);
			return new CollectResponse(session, Status.Error, str);
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

		tt.callTwint(collectRequest, session);

		return new CollectResponse(session, collectService.getCollectInfo(session).getStatus(), "");
	}

	@ApiOperation(value = "Trigger a status check")
	@RequestMapping(path = "/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody StatusResponse status(@RequestBody StatusRequest statusRequest){
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
