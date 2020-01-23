package com.afp.medialab.weverify.social.dao.service;

import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.model.CollectRequest;

/**
 * Search request cache management
 * @author Bertrand Goupil
 *
 * <pre>
 * Case: Get stored elasticsearch session-id for:
 *  <li> Same request
 *  <li> Same request + new date range
 *  <li> Same request + new user if user already set
 * </pre>
 * 
 */
@Service
public class CacheService {

	

	
	
	public String getRecordedSessionId(CollectRequest collectRequest) {
	
		String sessionId = null;
		
		return sessionId;
	}
	
}
