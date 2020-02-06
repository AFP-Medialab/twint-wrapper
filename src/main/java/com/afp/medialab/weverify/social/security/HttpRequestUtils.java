/**
 * 
 */
package com.afp.medialab.weverify.social.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

/**
 * Utils class to extract information from HttpServletRequest.
 * 
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
public class HttpRequestUtils {

	private static final String[] IP_HEADER_CANDIDATES = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
			"HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
			"HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" };

	private static final ThreadLocal<UserAgentAnalyzer> USER_AGENT_ANALYZER_TL = new ThreadLocal<UserAgentAnalyzer>() {
		@Override
		protected UserAgentAnalyzer initialValue() {
			return createUserAgentAnalyzer();
		}
	};

	/**
	 * @return
	 */
	public static String getClientIpAddress() {
		if (RequestContextHolder.getRequestAttributes() == null) {
			// return "0.0.0.0";
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		for (String header : IP_HEADER_CANDIDATES) {
			String ipList = request.getHeader(header);
			if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
				String ip = ipList.split(",")[0];
				return ip;
			}
		}

		return request.getRemoteAddr();
	}

	/**
	 * @return
	 */
	public static Map<String, String> getUserAgentInformation() {
		if (RequestContextHolder.getRequestAttributes() == null) {
			return Collections.emptyMap();
		}
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String userAgentHeader = request.getHeader("User-Agent");
		if (userAgentHeader == null || userAgentHeader.trim().length() == 0) {
			return Collections.emptyMap();
		}
		UserAgent ua = USER_AGENT_ANALYZER_TL.get().parse(userAgentHeader);
		Map<String, String> uaInfo = new HashMap<String, String>();
		ua.getAvailableFieldNames().stream().map(field -> uaInfo.put(field, ua.getValue(field)));
		return Collections.unmodifiableMap(uaInfo);
	}

	// No constructor.
	private HttpRequestUtils() {
		super();
	}

	private static UserAgentAnalyzer createUserAgentAnalyzer() {
		UserAgentAnalyzer uaa = UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().withField("DeviceClass")
				.withField("DeviceName").withField("DeviceVersion").withField("LayoutEngineClass")
				.withField("LayoutEngineName").withField("LayoutEngineVersion").withField("AgentClass")
				.withField("AgentName").withField("AgentVersion").withField("AgentVersionMajor")
				.withField("OperatingSystemClass").withField("OperatingSystemName").withField("OperatingSystemVersion")
				.withField("OperatingSystemVersionMajor").withCache(10000).build();
		return uaa;
	}
}
