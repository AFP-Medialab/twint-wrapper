package com.afp.medialab.weverify.social.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import net.minidev.json.JSONArray;

@Component
public class UserPropertiesManager {

	private static Logger Logger = LoggerFactory.getLogger(UserPropertiesManager.class);

	@Value("${application.twint.limit.min}")
	private int scrappingLimitMin;

	@Value("${application.twint.limit.medium}")
	private int scrappingLimitMedium;

	@Value("${application.twint.limit.max}")
	private int scrappingLimitMax;

	@Value("${application.twint.limit.default}")
	private int scrappingLimitDefault;

	private Jwt getJwtContext() {
		return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private JSONArray getUserRoles() {
		Jwt usrToken = getJwtContext();
		JSONArray roles = usrToken.getClaim("roles");
		return roles;
	}

	/**
	 * Test if user is allow to override the cache property
	 * 
	 * @return
	 */
	public boolean isAllowOverrideCache() {
		JSONArray roles = getUserRoles();
		boolean isallow = roles.contains("CACHEOVERRIDE");
		return isallow;
	}

	/**
	 * Get scraping limit per user Role.
	 * 
	 * @return scrap limit
	 */
	public int getLimitFromUserRole() {
		JSONArray roles = getUserRoles();
		String userRole = "";
		for (Object role : roles) {
			String strRole = (String) role;
			if (strRole.equals("MAXLIMIT")) {
				userRole = strRole;
				break;
			} else if (strRole.equals("MEDIUMLIMIT")) {
				userRole = strRole;
			} else if (strRole.equals("LIGHTLIMIT") && userRole.equals(""))
				userRole = strRole;
		}

		Logger.info("user role {}", userRole);

		if (userRole.equals("MAXLIMIT"))
			return scrappingLimitMax;
		else if (userRole.equals("MEDIUMLIMIT"))
			return scrappingLimitMedium;
		else if (userRole.equals("LIGHTLIMIT"))
			return scrappingLimitMin;
		else
			return scrappingLimitDefault;
	}

}
