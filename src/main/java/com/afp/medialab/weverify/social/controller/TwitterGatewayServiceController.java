package com.afp.medialab.weverify.social.controller;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import com.afp.medialab.weverify.social.util.RequestCacheManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "Twitter scraping API")
public class TwitterGatewayServiceController {

	private static Logger Logger = LoggerFactory.getLogger(TwitterGatewayServiceController.class);

	@Autowired
	private CollectService collectService;

	@Autowired
	private RequestCacheManager cacheService;

	@Value("${application.home.msg}")
	private String homeMsg;

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public @ResponseBody String home() {
		return homeMsg;
	}

	@ApiOperation(value = "Trigger a Twitter Scraping")
	@RequestMapping(path = "/collect", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody CollectResponse collect(@RequestBody @Valid CollectRequest collectRequest,
			BindingResult result) {

		if (!collectRequest.isValid())
			return new CollectResponse(null, Status.Error,
					"and_list or user_list must be given, from and until are mandatory", null);

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

		return cacheService.useCache(collectRequest);
	}

	@ApiOperation(value = "Trigger a status check")
	@RequestMapping(path = "/status", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody StatusResponse status(@RequestBody StatusRequest statusRequest) {
		Logger.debug("POST status " + statusRequest.getSession());
		return getStatusResponse(statusRequest.getSession());
	}

	@ApiOperation(value = "Trigger a status check")
	@RequestMapping(path = "/status/{id}", method = RequestMethod.GET)
	public @ResponseBody StatusResponse status(@PathVariable("id") String id) {
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
		if (collectHistory == null)
			throw new NotFoundException();

		List<Request> requests = collectHistory.getRequests();
		List<CollectRequest> collectRequests = new LinkedList<CollectRequest>();
		for (Request request : requests) {
			CollectRequest collectRequest = new CollectRequest(request);
			collectRequests.add(collectRequest);
		}

		if (collectHistory.getStatus() != Status.Done)
			return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(),
					collectHistory.getProcessEnd(), collectHistory.getStatus(), collectRequests, null, null);
		else
			return new StatusResponse(collectHistory.getSession(), collectHistory.getProcessStart(),
					collectHistory.getProcessEnd(), collectHistory.getStatus(), collectRequests,
					collectHistory.getCount(), collectHistory.getMessage());
	}

	@RequestMapping(value = "/collect-history", method = RequestMethod.GET)
	public @ResponseBody HistoryResponse collectHistory(
			@RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
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
	public @ResponseBody HistoryResponse status(@RequestBody @Valid HistoryRequest historyRequest) {
		Logger.info("POST collect-history : " + historyRequest.toString());
		List<CollectHistory> collectHistoryList = collectService.getHistory(historyRequest.getLimit(),
				historyRequest.getStatus(),
				(historyRequest.getSort() == null ? false : historyRequest.getSort().equals("desc")),
				historyRequest.getProcessStart(), historyRequest.getProcessTo());
		return new HistoryResponse(collectHistoryList);
	}

	@ApiOperation(value = "Update an old request")
	@RequestMapping(path = "/collect-update/{id}", method = RequestMethod.GET)
	public @ResponseBody StatusResponse collectUpdate(@PathVariable("id") String id)
			throws ExecutionException, InterruptedException, IOException {
		return collectUpdateFunction(id);
	}

	@ApiOperation(value = "Update an old request")
	@RequestMapping(path = "/collect-update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody StatusResponse collectUpdate(@RequestBody @Valid CollectUpdateRequest collectUpdateRequest)
			throws ExecutionException, InterruptedException, IOException {
		String id = collectUpdateRequest.getSession();
		return collectUpdateFunction(id);
	}

	/**
	 * @param session
	 * @return Status response or the corresponding session
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @func Calls Twint on the time interval of a session. The result replaces the
	 *       old ones un elastic search.
	 */
	private StatusResponse collectUpdateFunction(String session) throws ExecutionException, InterruptedException {
		Logger.info("Collect-Update " + session);

		CollectHistory collectHistory = collectService.getCollectInfo(session);
		if (collectHistory == null)
			throw new NotFoundException();

		if (collectHistory.getStatus() != Status.Done) {
			StatusResponse res = getStatusResponse(session);
			res.setMessage("This session is already updating");
			return res;
		}

		collectHistory.setStatus(Status.Pending);
		// Request request = collectHistory.getRequest();
		Request request = new Request();
		collectHistory.setProcessStart(new Date());
		collectService.save_collectHistory(collectHistory);
		collectService.callTwintOnInterval(collectHistory, request, request.getSince(), request.getUntil());
		return getStatusResponse(session);
	}
}