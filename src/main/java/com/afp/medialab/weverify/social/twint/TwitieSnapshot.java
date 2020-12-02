package com.afp.medialab.weverify.social.twint;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afp.medialab.weverify.social.model.twint.TwintModel;

/**
 * Implementation for the legacy version
 * 
 * @author Bertrand Goupil
 *
 */
public class TwitieSnapshot implements ITwitieProcess {
	private static Logger Logger = LoggerFactory.getLogger(TwitieSnapshot.class);

	@Override
	public Object buildTwitieRequest(TwintModel twintModel) {
		Logger.debug("use legacy Twitie request format");
		String tweet = StringUtils.normalizeSpace(twintModel.getFull_text());
		return tweet;
	}

}
