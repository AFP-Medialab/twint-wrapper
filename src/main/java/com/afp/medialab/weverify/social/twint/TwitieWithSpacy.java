package com.afp.medialab.weverify.social.twint;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afp.medialab.weverify.social.model.twint.TwintModel;

import eu.elg.model.Markup;
import eu.elg.model.requests.TextRequest;

public class TwitieWithSpacy implements ITwitieProcess {
	private static Logger Logger = LoggerFactory.getLogger(TwitieWithSpacy.class);

	@Override
	public Object buildTwitieRequest(TwintModel twintModel) {
		Logger.debug("use Twitie' spacy based request");
		String tweet = StringUtils.normalizeSpace(twintModel.getFull_text());
		TextRequest req = new TextRequest().withContent(tweet)
				.withMarkup(new Markup().withFeature("lang", twintModel.getLang())).withParams(Collections.singletonMap(
						"annotations", Arrays.asList(":Person", ":UserID", ":Location", ":Organization")));
		return req;
	}

}
