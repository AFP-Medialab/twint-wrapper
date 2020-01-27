package com.afp.medialab.weverify.social.dao.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.entity.Request;
import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.dao.repository.RequestInterface;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;
import com.afp.medialab.weverify.social.twint.TwintThreadGroup;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CollectService {

	private static Logger Logger = LoggerFactory.getLogger(CollectService.class);
	@Autowired
	private CollectInterface collectInterface;

	@Autowired
	private RequestInterface requestInterface;

	@Autowired
	private TwintThreadGroup ttg;

	public CollectRequest stringToCollectRequest(String query) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			CollectRequest collectRequest = mapper.readValue(query, CollectRequest.class);
			return collectRequest;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

//	public CollectHistory saveCollectInfo(String session, CollectRequest collectRequest, Date processStart,
//			Date processEnd, Status status, String message, Integer count, Integer finished_threads,
//			Integer total_threads, Integer successful_threads) {
//		CollectHistory collectHistory = new CollectHistory(session, new Request(collectRequest), processStart,
//				processEnd, status, message, count, finished_threads, total_threads, successful_threads);
//		return collectInterface.save(collectHistory);
//	}

//	public CollectResponse alreadyExists(CollectRequest collectRequest) {
//		Request request = null;
//		CollectHistory collectHistory = collectInterface.findCollectHistoryByRequest(request);
//		if (collectHistory != null)
//			return new CollectResponse(collectHistory.getSession(), collectHistory.getStatus(), null,
//					collectHistory.getProcessEnd());
//		return null;
//	}

	public Boolean updateCollectStatus(String session, Status newStatus) {
		CollectHistory collectHistory = collectInterface.findCollectHistoryBySession(session);
		Status existingStatus = collectHistory.getStatus();
		if (newStatus == Status.Pending && existingStatus == Status.Done) {
			collectInterface.updateCollectProcessEnd(session, null);
			collectInterface.updateCollectStatus(session, newStatus.toString());
			if (collectHistory.getTotal_threads() == null) {
				collectInterface.updateCollectTotal_threads(session, 0);
				collectInterface.updateCollectFinished_threads(session, 0);
				collectInterface.updateCollectSuccessful_threads(session, 0);
			}
			return true;
		}
		if (newStatus == Status.Running && existingStatus == Status.Pending) {
			if (collectHistory.getProcessStart() == null)
				collectInterface.updateCollectProcessStart(session, new Date());
			collectInterface.updateCollectStatus(session, newStatus.toString());
			return true;
		} else if (newStatus == Status.Done && existingStatus == Status.Running) {
			collectInterface.updateCollectProcessEnd(session, new Date());
			collectInterface.updateCollectStatus(session, newStatus.toString());
			return true;
		} else if (newStatus == Status.Error && existingStatus != Status.Error) {
			collectInterface.updateCollectProcessEnd(session, new Date());
			collectInterface.updateCollectStatus(session, newStatus.toString());
			return true;
		}
		return false;
	}

//	public void updateCollectQuery(String session, CollectRequest collectRequest) {
//		CollectHistory collectHistory = collectInterface.findCollectHistoryBySession(session);
//		Request request = collectHistory.getRequest();
//		request.update(collectRequest);
//		requestInterface.save(request);
//	}

	public void updateCollectProcessEnd(String session, Date date) {
		collectInterface.updateCollectProcessEnd(session, date);
	}

	public void updateCollectProcessStart(String session, Date date) {
		collectInterface.updateCollectProcessStart(session, date);
	}

	public CollectHistory getCollectInfo(String session) {
		return collectInterface.findCollectHistoryBySession(session);
	}

	public List<CollectHistory> getLasts(int nb, boolean desc) {
		List<CollectHistory> collectHistoryList = collectInterface.findAll();
		if (desc)
			Collections.reverse(collectHistoryList);

		if (collectHistoryList.size() >= nb)
			collectHistoryList = collectHistoryList.subList(0, nb);
		return collectHistoryList;
	}

	public List<CollectHistory> getAll(boolean desc) {
		List<CollectHistory> collectHistoryList = collectInterface.findAll();
		if (desc)
			Collections.reverse(collectHistoryList);

		return collectHistoryList;
	}

	public List<CollectHistory> getByStatus(String status, int limit, boolean desc) {
		List<CollectHistory> collectHistoryList = collectInterface.findCollectHistoryByStatus(status);
		if (desc)
			Collections.reverse(collectHistoryList);

		if (collectHistoryList.size() >= limit)
			collectHistoryList = collectHistoryList.subList(0, limit);
		return collectHistoryList;
	}

	public List<CollectHistory> getHistory(int limit, String status, boolean desc, Date processStart, Date processEnd) {
		List<CollectHistory> collectHistoryList = null;
		if (status != null && processEnd != null && processStart != null)
			collectHistoryList = collectInterface
					.findCollectHistoryByProcessEndLessThanEqualOrProcessEndIsNullAndProcessStartGreaterThanEqualAndStatus(
							processEnd, processStart, status);
		else if (status != null && processEnd == null && processStart == null)
			collectHistoryList = collectInterface.findCollectHistoryByStatus(status);
		else if (status != null && processEnd != null)
			collectHistoryList = collectInterface.findCollectHistoryByStatusAndProcessEndLessThan(status, processEnd);
		else if (status != null)
			collectHistoryList = collectInterface.findCollectHistoryByStatusAndProcessStartGreaterThan(status,
					processStart);
		else if (processEnd != null)
			collectHistoryList = collectInterface.findCollectHistoryByProcessEndLessThan(processEnd);
		else if (processStart != null)
			collectHistoryList = collectInterface.findCollectHistoryByProcessStartGreaterThan(processStart);
		else {

			collectHistoryList = collectInterface.findAll();

		}
		if (desc)
			Collections.reverse(collectHistoryList);

		if (limit != 0 && collectHistoryList.size() > limit)
			collectHistoryList = collectHistoryList.subList(0, limit);
		return collectHistoryList;
	}

	public void updateCollectMessage(String session, String message) {
		collectInterface.updateCollectMessage(session, message);
	}

	public void updateCollectCount(String session, Integer count) {
		collectInterface.updateCollectCount(session, count);
	}

	public void updateCollectFinishedThreads(String session, Integer finished_threads) {
		collectInterface.updateCollectFinished_threads(session, finished_threads);
	}

	public void updateCollectTotalThreads(String session, Integer total_threads) {
		collectInterface.updateCollectTotal_threads(session, total_threads);
	}

	public void updateCollectSuccessfulThreads(String session, Integer sucessful_threads) {
		collectInterface.updateCollectSuccessful_threads(session, sucessful_threads);
	}

	/**
	 * @param keywords
	 * @return Set<Request> or null if the given list is empty or null
	 * @function isContainedKeywords : This gives the list of requests that contains
	 *           the same or less words than given list.
	 */
	public Set<Request> requestsContainingOnlySomeOfTheKeywords(Set<String> keywords) {
		Set<Request> matching_keyWords = new HashSet<Request>();
		if (keywords == null || keywords.size() == 0)
			return null;
		for (String keyword : keywords) {
			List<Request> collected = requestInterface.my_findMatchingRequestByKeyword(keyword, keywords.size());
			matching_keyWords.addAll(collected);
		}

		return matching_keyWords.stream().filter(e -> e.getKeywordList().stream().anyMatch(keywords::contains))
				.collect(Collectors.toSet());
	}

	/**
	 * @param bannedWords
	 * @return Set<Request> or null if the given list is empty or null
	 * @function isContainedBannedWords : This gives the list of requests that
	 *           contains the same or less words than given list.
	 */
	public Set<Request> requestContainingOnlySomeOfTheBannedWords(Set<String> bannedWords) {
		Set<Request> matching_bannedWords = new HashSet<Request>();
		if (bannedWords == null || bannedWords.size() == 0)
			return null;
		for (String bannedWord : bannedWords) {
			List<Request> collected = requestInterface.my_findMatchingRequestByBannedWords(bannedWord,
					bannedWords.size());
			matching_bannedWords.addAll(collected);
		}
		matching_bannedWords = matching_bannedWords.stream()
				.filter(e -> e.getBannedWords().stream().anyMatch(bannedWords::contains)).collect(Collectors.toSet());
		matching_bannedWords.addAll(requestInterface.findRequestByBannedWordsIsNull());
		return matching_bannedWords;
	}

	public Set<Request> requestContainingAllTheUsers(Set<String> users) {
		Set<Request> matching_users = new HashSet<Request>();
		if (users == null || users.size() == 0)
			return null;
		for (String user : users) {
			List<Request> collected = requestInterface.my_findMatchingRequestByUsers(user, users.size());
			matching_users.addAll(collected);
		}
		matching_users = matching_users.stream()
				.filter(e -> e.getUserList().containsAll(users) || e.getUserList().size() == 0)
				.collect(Collectors.toSet());
		matching_users.addAll(requestInterface.findRequestByUserListIsNull());
		return matching_users;
	}

	public Set<Request> requestContainingEmptyUsers() {
		return new HashSet<Request>(requestInterface.findRequestByUserListIsNull());
	}

	public Set<Request> requestContainingEmptyBannedWords() {
		return new HashSet<Request>(requestInterface.findRequestByBannedWordsIsNull());
	}
//
//	public CollectHistory findCollectHistoryByRequest(Request request) {
//		return collectInterface.findCollectHistoryByRequest(request);
//	}

	public void save_collectHistory(CollectHistory collectHistory) {
		collectInterface.save(collectHistory);
	}

	public void save_request(Request request) {
		requestInterface.save(request);
	}

	public Set<Request> requestsContainingAllTheKeywords(Set<String> keywordList, String language) {
		Set<Request> matchingRequests = new HashSet<Request>();
		if (keywordList == null || keywordList.size() == 0)
			return null;
		for (String keyword : keywordList) {
			List<Request> collected = requestInterface.my_findSmallerRequestByKeyword(keyword, keywordList.size(),
					language);
			matchingRequests.addAll(collected);
		}
		return matchingRequests.stream().filter(e -> e.getKeywordList().containsAll(keywordList))
				.collect(Collectors.toSet());
	}

	public Set<Request> requestContainingAllTheBannedWords(Set<String> bannedWords) {
		Set<Request> matchingRequests = new HashSet<Request>();
		if (bannedWords == null || bannedWords.size() == 0)
			return null;
		for (String keyword : bannedWords) {
			List<Request> collected = requestInterface.my_findSmallerRequestByBannedWords(keyword, bannedWords.size());
			matchingRequests.addAll(collected);
		}
		return matchingRequests.stream().filter(e -> e.getKeywordList().containsAll(bannedWords))
				.collect(Collectors.toSet());
	}

	public Set<Request> requestsContainingOnlySomeOfTheUsers(Set<String> userList) {
		Set<Request> matching_users = new HashSet<Request>();
		if (userList == null || userList.size() == 0)
			return null;
		for (String user : userList) {
			List<Request> collected = requestInterface.my_findSmallerRequestByUsers(user);
			matching_users.addAll(collected);
		}
		matching_users = matching_users.stream().filter(e -> e.getBannedWords().stream().anyMatch(userList::contains))
				.collect(Collectors.toSet());
		return matching_users;
	}

	/**
	 * @param from
	 * @param until
	 * @return
	 * @func callTwintOnInterval overload with only one date interval. call Twint on
	 *       the interval and append the result to the elastic search session
	 */
	public void callTwintOnInterval(CollectHistory collectHistory, Request request, Date from, Date until) {
		CollectRequest newCollectRequest = new CollectRequest(request);
		newCollectRequest.setFrom(from);
		newCollectRequest.setUntil(until);
		ttg.callTwintMultiThreaded(collectHistory, newCollectRequest);
	}

	// New simplified methods

	public Set<Request> requestContainsKeyWords(Set<String> keywords) {
		if (keywords == null || keywords.size() == 0)
			return null;
		return requestInterface.findByKeywordListIn(keywords);
	}

	public Set<Request> requestContainsBannedKeyWords(Set<String> bannedWords) {
		if (bannedWords == null || bannedWords.size() == 0)
			return null;
		return requestInterface.findByBannedWordsIn(bannedWords);
	}

	public Set<Request> requestContainsUserList(Set<String> userList) {
		if (userList == null || userList.size() == 0)
			return null;
		return requestInterface.findByUserListIn(userList);
	}

	/**
	 * Create a new collecthistory
	 * 
	 * @return
	 */
	public CollectHistory createNewCollectHistory() {
		CollectHistory collectHistory = new CollectHistory();
		String session = UUID.randomUUID().toString();
		Logger.debug(session);
		collectHistory.setSession(session);
		collectHistory.setProcessStart(Calendar.getInstance().getTime());
		collectHistory.setStatus(Status.Pending);
		return collectHistory;

	}
}
