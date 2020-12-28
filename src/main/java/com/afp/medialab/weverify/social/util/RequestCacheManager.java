package com.afp.medialab.weverify.social.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
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
import com.afp.medialab.weverify.social.model.NotFoundException;
import com.afp.medialab.weverify.social.model.Status;
import com.afp.medialab.weverify.social.model.StatusResponse;
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
	
	@Autowired
	private UserPropertiesManager userPropertiesManager;

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
		Set<Request> previousMatch = exactRequests(similarRequests, collectRequest);
		boolean isAllowOverride = userPropertiesManager.isAllowOverrideCache();
		
		if(!isAllowOverride && !collectRequest.isCached()) {
			//user is not allow to override the cache => cache is set to true
			collectRequest.setCached(true);
		}
		if ((previousMatch != null && !previousMatch.isEmpty()) && collectRequest.isCached()) {
			// Requests exist in cache
			// Try to extend with new dateRange if so
			collectHistory = (collectRequest.isDisableTimeRange() ? reusePreviousRequest(previousMatch)
					: completeTimeRanges(collectHistory, collectRequest, previousMatch));
		} else {
			runNewTwintRequest(collectHistory, collectRequest);
			mergeRequest(similarRequests, collectRequest);
		}
		CollectResponse collectResponse = new CollectResponse(collectHistory);
		// This is a new request
		return collectResponse;
	}

	private CollectHistory reusePreviousSessionId(Set<Request> previousMatch) {

		if (previousMatch.isEmpty())
			return null;
		List<Request> requests = previousMatch.stream().filter(distinctByKeys(Request::getCollectHistory))
				.collect(Collectors.toList());

		if (requests.size() > 1)
			Logger.warn("Similar Request have several collect Id");

		CollectHistory collectHistoryReuse = requests.get(0).getCollectHistory();
		CollectHistory collectHistory = collectService.createNewCollectHistory(collectHistoryReuse.getSession());

		return collectHistory;
	}

	@SafeVarargs
	private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
		final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();

		return t -> {
			final List<?> keys = Arrays.stream(keyExtractors).map(ke -> ke.apply(t)).collect(Collectors.toList());

			return seen.putIfAbsent(keys, Boolean.TRUE) == null;
		};
	}

	/**
	 * Merge previous request to the new request. Example prev-request = 'Fake' &
	 * 'News' => new-request= 'Fake' prev-request is merge to new-request because
	 * new-request is larger.
	 * 
	 * @param mergeCandidates
	 * @param collectRequest
	 */
	private void mergeRequest(Set<Request> mergeCandidates, CollectRequest collectRequest) {
		if (mergeCandidates != null && !mergeCandidates.isEmpty()) {
			for (Request request : mergeCandidates) {
				String firtLang = request.getLanguage();
				if ((firtLang != null && !collectRequest.getLang().equals(""))
						&& (firtLang != null && !firtLang.equals(collectRequest.getLang()))) {
					Logger.info("nothing to merge");
				} else {
					request.setMerge(true);
					collectService.save_request(request);
				}
			}
		}

	}

	/**
	 * Extract exact match previous requests
	 * 
	 * @param request
	 * @param collectRequest
	 * @return
	 */
	private Set<Request> exactRequests(Set<Request> request, CollectRequest collectRequest) {
		Set<Request> filterRequest = new HashSet<Request>();
		// Set<Request> filterRequest1 = new HashSet<Request>();
		if (request.isEmpty())
			return filterRequest;

		final Set<String> keywords;
		final Set<String> userList;
		final Set<String> banneWords;
		final String lang;
		if (collectRequest.getKeywordList() == null) {
			keywords = new TreeSet<String>();
			collectRequest.setKeywordList((SortedSet<String>) keywords);
		} else {
			keywords = collectRequest.getKeywordList();
		}
		if (collectRequest.getUserList() == null) {
			userList = new TreeSet<String>();
			collectRequest.setUserList((SortedSet<String>) userList);
		} else {
			userList = collectRequest.getUserList();
		}
		if (collectRequest.getBannedWords() == null) {
			banneWords = new TreeSet<String>();
			collectRequest.setBannedWords((SortedSet<String>) banneWords);
		} else
			banneWords = collectRequest.getBannedWords();
		if (collectRequest.getLang() == null) {
			lang = "";
			collectRequest.setLang(lang);
		} else {
			lang = collectRequest.getLang();
		}

		filterRequest.addAll(request.stream().filter(e -> e.getKeywordList().stream().allMatch(keywords::contains))
				.filter(e -> e.getUserList().stream().allMatch(userList::contains))
				.filter(e -> e.getBannedWords().stream().allMatch(banneWords::contains))
				.filter(e -> e.getVerified().equals(collectRequest.isVerified())).collect(Collectors.toSet()));

		Set<Request> filterRequest2 = new HashSet<Request>();
		filterRequest2.addAll(filterRequest);
		for (Request filterReq : filterRequest2) {
			String firtLang = filterReq.getLanguage();
			if ((firtLang != null && !lang.equals("")) && (firtLang != null && !firtLang.equals(lang)))
				filterRequest.remove(filterReq);
		}
		// if requests is empty search if a largers request exist
		// e.g if one keywords => requested is 2 keywords, send reuse the one keywords
		// if one keywords, no user => request 1 keyword + 1 users => reuse 1 kewords

		return filterRequest;
	}

	/**
	 * Get requests match same keywords Example: new-request = 'Fake' => similiar:
	 * prev-request: 'Fake' & 'News'
	 * 
	 * @param collectRequest
	 * @return
	 */
	private Set<Request> similarInCache(CollectRequest collectRequest) {
		Set<Request> requestSameKeyWords = collectService.requestContainsKeyWords(collectRequest.getKeywordList());
		Set<Request> machingRequests = mergeCriteria(requestSameKeyWords);
		if (!collectRequest.isDisableTimeRange())
			machingRequests = rangeDeltaToProcess.requestfromDateRange(machingRequests, collectRequest);
		return machingRequests;
	}

	/**
	 * Add similar request as merge candidate if status is not in error
	 * 
	 * @param requestSameKeyWords
	 * @return
	 */
	private Set<Request> mergeCriteria(Set<Request> requestSameKeyWords) {
		Set<Request> machingRequests = new HashSet<Request>();
		if (requestSameKeyWords != null && requestSameKeyWords.size() > 0) {
			machingRequests.addAll(requestSameKeyWords);
		}
		machingRequests.removeIf(e -> e.getCollectHistory().getStatus().equals(Status.Error));
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
			Logger.info("Process twint ranges : " + rangesToProcess.size());
			for (DateRange range : rangesToProcess) {
				CollectRequest request = runNewTwintRequest(collectHistory, collectRequest, range);
				requestsToPerform.add(request);
			}
			collectService.save_collectHistory(collectHistory);
			int scrappingLimit = userPropertiesManager.getLimitFromUserRole();
			ttg.callTwintMultiThreaded(collectHistory, requestsToPerform, scrappingLimit);

		} else {
			collectHistory = reusePreviousRequest(requests);
		}

		return collectHistory;
	}

	private CollectHistory reusePreviousRequest(Set<Request> requests) {
		CollectHistory collectHistory = reusePreviousSessionId(requests);
		// Request have been done already these elk
		collectHistory.setStatus(Status.Done);
		collectHistory.setMessage("Request already processed, no scrapping, see search engine");
		collectHistory.setProcessEnd(Calendar.getInstance().getTime());
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
		CollectRequest newCollect = new CollectRequest(collectRequest);
		newCollect.setFrom(dateRange.getStartDate());
		newCollect.setUntil(dateRange.getEndDate());
		Request request = new Request(newCollect);
		collectHistory.addRequest(request);
		return newCollect;
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
		int scrappingLimit = userPropertiesManager.getLimitFromUserRole();
		ttg.callTwintMultiThreaded(collectHistory, collectRequest, scrappingLimit);
	}
	
	/**
	 * @param session
	 * @return Status response or the corresponding session
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @func Calls Twint on the time interval of a session. The result replaces the
	 *       old ones un elastic search.
	 */
	public StatusResponse collectUpdateFunction(String session) throws ExecutionException, InterruptedException {
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
		collectHistory.setProcessStart(new Date());
		collectService.save_collectHistory(collectHistory);
		callTwint(collectHistory);

		return getStatusResponse(session);
	}
	
	/**
	 * Update existing request
	 * @param collectHistory
	 */
	private void callTwint(CollectHistory collectHistory) {

		List<Request> requests = collectHistory.getRequests();
		int scrappingLimit = userPropertiesManager.getLimitFromUserRole();
		for (Request request : requests) {
			CollectRequest newCollectRequest = new CollectRequest(request);
			ttg.callTwintMultiThreaded(collectHistory, newCollectRequest, scrappingLimit);
		}

	}
	
	/**
	 * @param session we want the status from.
	 * @return StatusResponse of the session.
	 * @func Returns the status response of a given session.
	 */
	public StatusResponse getStatusResponse(String session) {
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

}
