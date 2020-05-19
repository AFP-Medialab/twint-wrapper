package com.afp.medialab.weverify.social.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@EnableWebSecurity
/**
 * Set 2 security level for operation access and Actuator
 * @author bertrand
 *
 */
public class MultiSecurityConfig {

	@Value("${application.actuator.user}")
	private String actuatorUser;

	@Value("${application.actuator.passwd}")
	private String actuatorPassword;

	@Bean
	public UserDetailsService userDetailsService() throws Exception {
		UserDetails user = User.withUsername(actuatorUser).password(actuatorPassword)
				.roles("ACTUATOR").build();
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(user);
		return manager;
	}

	@Configuration
	/**
	 * Fusion Auth OAuth2 token
	 * @author Bertrand Goupil
	 *
	 */
	public static class FusionSecurityConfig extends WebSecurityConfigurerAdapter {

		private static final String[] AUTH_WHITELIST = {
				// -- Swagger ui
				"/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**"
				// -- Authentication services
				, "/api/v1/auth/registration", "/api/v1/auth/accesscode", "/api/v1/auth/login",
				"/api/v1/auth/refreshtoken", "/api/v1/auth/logout" };

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable().authorizeRequests(authorizeRequests -> authorizeRequests.antMatchers(AUTH_WHITELIST)
					.permitAll().anyRequest().authenticated()).oauth2ResourceServer().jwt();
		}
	}

	@Configuration
	@Order(1)
	/**
	 * Actuator Basic Auth
	 * @author Bertrand Goupil
	 *
	 */
	public static class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/manage/**").authorizeRequests().anyRequest().hasRole("ACTUATOR").and().httpBasic();
		}
	}

}
