package com.afp.medialab.weverify.social.util;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		// If exist exactly the same request ?

		CollectHistory collectHistory = collectService.createNewCollectHistory();
		Set<Request> similarRequests = similarInCache(collectRequest);

		// Set<Request> previousMatch = requestIsInCache(collectRequest);
		Set<Request> previousMatch = exactRequests(similarRequests, collectRequest);
		if (previousMatch != null && !previousMatch.isEmpty()) {
			// Requests exist in cache
			// Try to extend
			completeTimeRanges(collectHistory, collectRequest, previousMatch);
		} else {
			// Set<Request> similarRequests = similarInCache(collectRequest);

			runNewTwintRequest(collectHistory, collectRequest);
			mergeRequest(similarRequests, collectRequest);
//			if (similarRequests != null && !similarRequests.isEmpty()) {
//				// Merge old requests
//				for (Request request : similarRequests) {
//					request.setMerge(true);
//					collectService.save_request(request);
//				}
//			}

		}
		CollectResponse collectResponse = new CollectResponse(collectHistory);
		// This is a new request
		return collectResponse;
	}

	private void mergeRequest(Set<Request> mergeCandidates, CollectRequest collectRequest) {
		if (mergeCandidates != null && !mergeCandidates.isEmpty()) {
			Set<Request> filterRequest = new HashSet<Request>();
			filterRequest.addAll(mergeCandidates);
			Set<String> keywords = collectRequest.getKeywordList();
			Set<String> userList = collectRequest.getUserList();
			Set<String> banneWords = collectRequest.getBannedWords();

			if (mergeCandidates != null && !mergeCandidates.isEmpty()) {
				if (keywords != null) {
					filterRequest.addAll(filterRequest.stream().filter(e -> e.getKeywordList().size() < keywords.size())
							.collect(Collectors.toSet()));
				}
				if (userList != null) {
					filterRequest.addAll(
							filterRequest.stream().filter(e -> e.getUserList().stream().allMatch(userList::contains))
									.collect(Collectors.toSet()));
				}
				if (banneWords != null) {
					filterRequest.addAll(filterRequest.stream()
							.filter(e -> e.getBannedWords().stream().allMatch(banneWords::contains))
							.collect(Collectors.toSet()));
				}
			}
			for (Request request : filterRequest) {
				request.setMerge(true);
				collectService.save_request(request);
			}
		}

	}

	private Set<Request> exactRequests(Set<Request> request, CollectRequest collectRequest) {
		Set<Request> filterRequest = new HashSet<Request>();
		if (request.isEmpty())
			return filterRequest;

		final Set<String> keywords;
		final Set<String> userList;
		final Set<String> banneWords;

		if (collectRequest.getKeywordList() == null) {
			keywords = new HashSet<String>();
		} else {
			keywords = collectRequest.getKeywordList();
		}
		if (collectRequest.getUserList() == null) {
			userList = new HashSet<String>();
		} else {
			userList = collectRequest.getUserList();
		}
		if(collectRequest.getBannedWords() == null)
			banneWords = new HashSet<String>();
		else
			banneWords = collectRequest.getBannedWords();

		filterRequest.addAll(request.stream().filter(e -> e.getKeywordList().stream().allMatch(keywords::contains))
				.filter(e -> e.getUserList().stream().allMatch(userList::contains))
				.filter(e -> e.getBannedWords().stream().allMatch(banneWords::contains)).collect(Collectors.toSet()));

		return filterRequest;
	}

	/**
	 * 
	 * @param collectRequest
	 * @return
	 */
	private Set<Request> requestIsInCache(CollectRequest collectRequest) {
		Set<Request> requestSameKeyWords = collectService
				.requestsContainingOnlySomeOfTheKeywords(collectRequest.getKeywordList());
		Set<Request> bannedWords = collectService
				.requestContainingOnlySomeOfTheBannedWords(collectRequest.getBannedWords());
		Set<Request> users = collectService.requestContainingAllTheUsers(collectRequest.getUserList());
		Set<Request> machingRequests = mergeCriteria(requestSameKeyWords, bannedWords, users);
		return machingRequests;

	}

	private Set<Request> similarInCache(CollectRequest collectRequest) {
		Set<Request> requestSameKeyWords = collectService.requestContainsKeyWords(collectRequest.getKeywordList());
		Set<Request> bannedWords = collectService.requestContainsBannedKeyWords(collectRequest.getBannedWords());
		Set<Request> users = collectService.requestContainsUserList(collectRequest.getUserList());
		Set<Request> machingRequests = mergeCriteria(requestSameKeyWords, bannedWords, users);
		rangeDeltaToProcess.requestfromDateRange(machingRequests, collectRequest);
		return machingRequests;
	}

	private Set<Request> mergeCriteria(Set<Request> requestSameKeyWords, Set<Request> bannedWords, Set<Request> users) {
		Set<Request> machingRequests = new HashSet<Request>();
		if (requestSameKeyWords != null && requestSameKeyWords.size() > 0) {
			machingRequests.addAll(requestSameKeyWords);
			if (bannedWords != null)
				machingRequests.retainAll(bannedWords);
			else
				machingRequests.retainAll(collectService.requestContainingEmptyBannedWords());
			if (users != null)
				machingRequests.retainAll(users);
			else
				machingRequests.retainAll(collectService.requestContainingEmptyUsers());
		} else {
			if (users != null)
				machingRequests = users;
		}
		if (machingRequests != null && !machingRequests.isEmpty())
			machingRequests = machingRequests.stream()
					.filter(e -> !e.getCollectHistory().getStatus().equals(Status.Error)).collect(Collectors.toSet());
		return machingRequests;
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
		List<CollectRequest> requestsToPerform = new LinkedList<CollectRequest>();
		// This is a new reques
		if (!rangesToProcess.isEmpty()) {
			// Process twint
			for (DateRange range : rangesToProcess) {

				CollectRequest request = runNewTwintRequest(collectHistory, collectRequest, range);
				requestsToPerform.add(request);
			}
			ttg.callTwintMultiThreaded(collectHistory, requestsToPerform);
			collectService.save_collectHistory(collectHistory);
		} else {
			// Request have been done already these elk
			collectHistory.setStatus(Status.Done);
			collectHistory.setMessage("Request already processed, no scrapping, see search engine");
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
	private CollectRequest runNewTwintRequest(CollectHistory collectHistory, CollectRequest collectRequest,
			DateRange dateRange) {

		collectRequest.setFrom(dateRange.getStartDate());
		collectRequest.setUntil(dateRange.getEndDate());
		Request request = new Request(collectRequest);
		collectHistory.addRequest(request);
		return collectRequest;
	}

	/**
	 * Run twin with a new request
	 * 
	 * @param collectRequest
	 */
	private void runNewTwintRequest(CollectHistory collectHistory, CollectRequest collectRequest) {

		Request request = new Request(collectRequest);
		collectHistory.addRequest(request);
		collectService.save_collectHistory(collectHistory);
		ttg.callTwintMultiThreaded(collectHistory, collectRequest);
	}

}