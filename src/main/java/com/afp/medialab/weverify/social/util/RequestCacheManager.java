package com.afp.medialab.weverify.social.util;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.entity.Request;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
import com.afp.medialab.weverify.social.twint.TwintThreadGroup;

@Component
public class RequestCacheManager {

	private static Logger Logger = LoggerFactory.getLogger(RequestCacheManager.class);

	@Autowired
	private CollectService collectService;

	@Autowired
	private RangeDeltaToProcess rangeDeltaToProcess;

	@Autowired
	private TwintThreadGroup ttg;

	public String getRecordedSessionId(CollectRequest collectRequest) {
		String sessionId = null;

		return sessionId;
	}

	/**
	 * @param collectRequest collect request asked.
	 * @return
	 * @func Verifies if the request has already been done. If not creates a new
	 *       session and gives a CollectResponse accordingly.
	 */
	public CollectResponse useCache(CollectRequest collectRequest) {
		// Get registered request for this new search
		CollectHistory collectHistory = collectService.createNewCollectHistory();
		Set<Request> previousMatch = requestIsInCache(collectRequest);

		if (previousMatch != null && !previousMatch.isEmpty()) {
			// Requests exist in cache
			// Try to extend
			completeTimeRanges(collectHistory, collectRequest, previousMatch);
		} else {
			runNewTwintRequest(collectHistory, collectRequest);
		}
		CollectResponse collectResponse = new CollectResponse(collectHistory);
		// This is a new request
		return collectResponse;
	}

	/**
	 * 
	 * @param collectRequest
	 * @return
	 */
	private Set<Request> requestIsInCache(CollectRequest collectRequest) {

		Set<Request> requestSameKeyWords = collectService.requestContainsKeyWords(collectRequest.getKeywordList());
		Set<Request> banned_words = collectService.requestContainsBannedKeyWords(collectRequest.getBannedWords());
		Set<Request> users = collectService.requestContainsUserList(collectRequest.getUserList());

		Set<Request> maching_requests = new HashSet<Request>();
		if (requestSameKeyWords != null && requestSameKeyWords.size() > 0) {
			maching_requests.addAll(requestSameKeyWords);
			if (banned_words != null)
				maching_requests.retainAll(banned_words);
			else
				maching_requests.retainAll(collectService.requestContainingEmptyBannedWords());
			if (users != null)
				maching_requests.retainAll(users);
			else
				maching_requests.retainAll(collectService.requestContainingEmptyUsers());
		} else {
			maching_requests = users;
		}
		return maching_requests;
	}

	private Set<Request> similarInCache(CollectRequest collectRequest) {
		Set<Request> keywords = collectService.requestsContainingAllTheKeywords(collectRequest.getKeywordList(),
				collectRequest.getLang());
		Set<Request> bannedWords = collectService.requestContainingAllTheBannedWords(collectRequest.getBannedWords());
		Set<Request> users = collectService.requestsContainingOnlySomeOfTheUsers(collectRequest.getUserList());

		Set<Request> maching_requests;
		if (keywords != null) {
			maching_requests = new HashSet<Request>(keywords);
			if (bannedWords != null)
				maching_requests.retainAll(bannedWords);
			if (users != null)
				maching_requests.retainAll(users);
		} else {
			maching_requests = users;
		}
		return maching_requests;
	}

	/**
	 * Build Ranges to Process.
	 * 
	 * @param collectRequest
	 * @param requests
	 * @return
	 */
	private CollectHistory completeTimeRanges(CollectHistory collectHistory, CollectRequest collectRequest,
			Set<Request> requests) {
		if (requests == null) {
			Logger.debug("No request in cache -> New request");
			return null;
		}
		// Create DateRangeList
		// Search for range that have not been process yet
		DateRange requestDateRange = new DateRange(collectRequest.getFrom(), collectRequest.getUntil());
		List<DateRange> existingDateRanges = new LinkedList<DateRange>();
		for (Request request : requests) {
			DateRange dateRange = new DateRange(request.getSince(), request.getUntil());
			existingDateRanges.add(dateRange);
		}
		List<DateRange> rangesToProcess = rangeDeltaToProcess.rangeToProcess(existingDateRanges, requestDateRange);
		List<CollectHistory> newCollects = new LinkedList<CollectHistory>();
		// This is a new reques
		if (!rangesToProcess.isEmpty()) {
			// Process twint
			for (DateRange range : rangesToProcess) {
				runNewTwintRequest(collectHistory, collectRequest, range);
				newCollects.add(collectHistory);
			}
		} else {
			// Request have been done already these elk
			collectHistory.setStatus(Status.Done);
			collectHistory.setMessage("Request already processed, not scrapping, see search engine");
			collectHistory.setProcessEnd(Calendar.getInstance().getTime());
		}
		return collectHistory;
	}

	/**
	 * Run twin with a new request
	 * 
	 * @param collectRequest
	 * @param dateRange
	 */
	private void runNewTwintRequest(CollectHistory collectHistory, CollectRequest collectRequest, DateRange dateRange) {

		collectRequest.setFrom(dateRange.getStartDate());
		collectRequest.setUntil(dateRange.getEndDate());
		runNewTwintRequest(collectHistory, collectRequest);
	}

	/**
	 * Run twin with a new request
	 * 
	 * @param collectRequest
	 */
	private void runNewTwintRequest(CollectHistory collectHistory, CollectRequest collectRequest) {

		Request request = new Request(collectRequest);
		collectHistory.addRequest(request);
		ttg.callTwintMultiThreaded(collectHistory, collectRequest);
	}

	private CollectResponse makeRequestFromListAndCompleteTime(CollectRequest collectRequest, Set<Request> requests) {
		if (requests == null)
			return null;
		for (Request request : requests) {
//			CollectResponse result = makeRequestIfDatesAreLarger(collectRequest, request);
//			if (result != null)
//				return result;
		}
		return null;
	}

	private CollectResponse makeLargerRequestFromListAndCompleteTime(CollectRequest collectRequest,
			Set<Request> requests) {
		for (Request request : requests) {
			// CollectResponse result = makeLargerRequestIfDatesAreLarger(collectRequest,
			// request);
			// if (result != null)
			// return result;
		}
		return null;
	}

}
