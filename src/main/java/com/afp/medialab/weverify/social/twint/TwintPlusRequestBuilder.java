package com.afp.medialab.weverify.social.twint;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afp.medialab.weverify.social.model.CollectRequest;

/**
 * https://developer.twitter.com/en/docs/tweets/rules-and-filtering/overview/standard-operators
 * 
 * @author Bertrand Goupil
 *
 */
public class TwintPlusRequestBuilder {

	private static Logger Logger = LoggerFactory.getLogger(TwintPlusRequestBuilder.class);

	private static final TwintPlusRequestBuilder INSTANCE = new TwintPlusRequestBuilder();

	public static TwintPlusRequestBuilder getInstance() {
		return INSTANCE;
	}

	public String generateRequest(CollectRequest cr, String id, boolean isDocker, String esURL) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//boolean hasUser = false;
		StringBuffer cmdBuff = new StringBuffer();

		cmdBuff.append("tplus -cq ");
		// search string
		cmdBuff.append(generateSearch(cr));
		// Date Range
		if (cr.getFrom() != null) {
			String fromStr = format.format(cr.getFrom());
			cmdBuff.append(" --since '" + fromStr + "'");
		}

		if (cr.getUntil() != null) {
			String untilStr = format.format(cr.getUntil());
			cmdBuff.append(" --until '" + untilStr + "'");
		}
		cmdBuff.append(" -ee " + esURL + " -es --index-name tsna");
		cmdBuff.append(" --essid " + id +"");
		String twintPlusCmd = cmdBuff.toString();
		if (isDocker)
			twintPlusCmd = " \"" + twintPlusCmd + "\"";
		Logger.debug("Twint command: {}", twintPlusCmd);
		return twintPlusCmd;
	}

	private String generateSearch(CollectRequest collectRequest) {
		if (collectRequest == null)
			return null;
		StringBuilder sb = new StringBuilder();

		if (collectRequest.getKeywordList() != null) {
			ArrayList<String> and = new ArrayList<String>(collectRequest.getKeywordList());
			sb.append(and.get(0));
			for (int i = 1; i < and.size(); i++) {
				sb.append(" " + and.get(i));
			}
		}

		if (collectRequest.getBannedWords() != null)
			for (String s : collectRequest.getBannedWords()) {
				sb.append(" -" + s);
			}
		if (collectRequest.getLang() != null && !collectRequest.getLang().equals(""))
			sb.append(" lang:" + collectRequest.getLang());
		if (collectRequest.getMedia() != null) {
			if (collectRequest.getMedia().equals("both"))
				sb.append(" filter:media");
			else if (collectRequest.getMedia().equals("image"))
				sb.append(" filter:images");
			else if (collectRequest.getMedia().equals("video"))
				sb.append(" filter:native_video");
		}
		// users
		if (collectRequest.getUserList() != null && !collectRequest.getUserList().isEmpty()) {
			// hasUser = true;
			String users = String.join("/", collectRequest.getUserList());
			if (collectRequest.getUserList().size() > 1)
				sb.append(" list:" + users);
			else
				sb.append(" from:" + users);
		}
		String encodedtwintplusCmd = encodeValue(sb.toString());
		Logger.debug("Twint command encoded: {}", encodedtwintplusCmd);
		return encodedtwintplusCmd;
	}
	
	private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
