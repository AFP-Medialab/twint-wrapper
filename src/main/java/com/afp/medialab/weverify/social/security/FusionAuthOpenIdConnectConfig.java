package com.afp.medialab.weverify.social.security;

import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import io.fusionauth.security.OpenIDAuthorizationCodeResourceDetails;

/**
 * FusionAuth OAuth workflow implementation. Based on fusionAuth property keys
 * 
 * @author Bertrand Goupil
 *
 */
@Configuration
@EnableOAuth2Client
public class FusionAuthOpenIdConnectConfig {

	@Value("${fusionAuth.accessTokenUri}")
	private String accessTokenUri;

	@Value("${fusionAuth.clientId}")
	private String clientId;

	@Value("${fusionAuth.clientSecret}")
	private String clientSecret;

	@Value("${fusionAuth.redirectUri}")
	private String redirectUri;

	@Value("${fusionAuth.userAuthorizationUri}")
	private String userAuthorizationUri;

	@Value("${fusionAuth.userInfoUri}")
	private String userInfoUri;

	@Bean
	public OpenIDAuthorizationCodeResourceDetails fusionAuthOpenId() {
		OpenIDAuthorizationCodeResourceDetails details = new OpenIDAuthorizationCodeResourceDetails();
		details.setClientId(clientId);
		details.setClientSecret(clientSecret);
		details.setAccessTokenUri(accessTokenUri);
		details.settUserInfoUri(userInfoUri);
		details.setUserAuthorizationUri(userAuthorizationUri);
		details.setScope(asList("openid", "email"));
		details.setPreEstablishedRedirectUri(redirectUri);
		details.setUseCurrentUri(false);
		details.setClientAuthenticationScheme(AuthenticationScheme.form);
		return details;
	}

	@Bean(name = "fusionRestTemplate")
	public OAuth2RestTemplate fusionAuthOpenIdTemplate(final OAuth2ClientContext clientContext) {
		return new OAuth2RestTemplate(fusionAuthOpenId(), clientContext);
	}

}
