package com.afp.medialab.weverify.social.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * Security configuration for fusionAuth
 * 
 * @author Bertrand Goupil
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 *
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String[] AUTH_WHITELIST = {
			// -- Swagger ui
			"/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**",
			// -- Authentication services
			"/api/v1/auth/user", "/api/v1/auth/accesscode", "/api/v1/auth/login", "/api/v1/auth/refreshtoken",
			"/api/v1/auth/logout" };

	// @Autowired
	// @Qualifier("fusionRestTemplate")
	// private OAuth2RestTemplate restTemplate;

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	private JwtRequestFilter filter;

	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers("/resources/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
				.addFilterAfter(new OAuth2ClientContextFilter(), AbstractPreAuthenticatedProcessingFilter.class)
				.authorizeRequests().antMatchers(AUTH_WHITELIST).permitAll().anyRequest().authenticated().and()
				.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
				// make sure we use stateless session; session won't be used to
				// store user's state.
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http.addFilterBefore(filter, OAuth2ClientContextFilter.class);
	}
}
