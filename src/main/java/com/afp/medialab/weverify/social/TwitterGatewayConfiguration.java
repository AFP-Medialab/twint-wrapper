package com.afp.medialab.weverify.social;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.afp.medialab.weverify.social.twint.ITwitieProcess;
import com.afp.medialab.weverify.social.twint.TwitieSnapshot;
import com.afp.medialab.weverify.social.twint.TwitieWithSpacy;

@Configuration
public class TwitterGatewayConfiguration {

	@Bean
	@ConditionalOnProperty(name = "application.twitie.isSpacyVersion", havingValue = "false")
	public ITwitieProcess getTwitieLegacyRequestBuilder() {
		return new TwitieSnapshot();
	}

	@Bean
	@ConditionalOnProperty(name = "application.twitie.isSpacyVersion", havingValue = "true", matchIfMissing = true)
	public ITwitieProcess getTwitieSpacyRequestBuilder() {
		return new TwitieWithSpacy();
	}

}
