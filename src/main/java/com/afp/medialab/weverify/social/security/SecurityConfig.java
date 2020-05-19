package com.afp.medialab.weverify.social.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Spring Security configuration based on JWT tokens.
 * 
 * @author Bertrand Goupil
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 *
 */
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
@Deprecated
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String[] AUTH_WHITELIST = {
			// -- Swagger ui
			"/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**"
			// -- Authentication services
			, "/api/v1/auth/registration", "/api/v1/auth/accesscode", "/api/v1/auth/login", "/api/v1/auth/refreshtoken",
			"/api/v1/auth/logout" };

	// @Override
	// public void configure(WebSecurity web) {
	// web.ignoring().antMatchers("/resources/**");
	// }

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers(AUTH_WHITELIST).permitAll().anyRequest().authenticated()
				.and().oauth2ResourceServer().jwt();
	}
}
