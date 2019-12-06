package com.afp.medialab.weverify.social.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fusionauth.security.FusionAuthUserDetails;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Value("${fusionAuth.userInfoUri}")
	private String userInfoUri;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String requestTokenHeader = request.getHeader("Authorization");
		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(requestTokenHeader);
				FusionAuthUserDetails user = new FusionAuthUserDetails(getUserInfo(requestTokenHeader), accessToken);
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						user, null, user.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}

		} else {
			// No Token
		}
		filterChain.doFilter(request, response);
	}

	private JsonNode getUserInfo(String requestTokenHeader) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", requestTokenHeader);
		HttpEntity<String> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<String> response = new RestTemplate().exchange(this.userInfoUri, HttpMethod.GET, httpEntity,
				String.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			return new ObjectMapper().readTree(response.getBody());
		}

		throw new BadCredentialsException("Failed to request user details from the UserInfo API. " + "Status code ["
				+ response.getStatusCodeValue() + "] Message [" + response.getBody() + "]");
	}

}
