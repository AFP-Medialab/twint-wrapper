/**
 * 
 */
package com.afp.medialab.weverify.social.security.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JwtLoginResponse implements Serializable {
	private static final long serialVersionUID = -6669175977712645213L;

	@ApiModelProperty(value = "User's JWT application token.", required = true,
			example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjdJekV4UlAteDI3TFhienNOYW5IMmdWUHZFQSJ9.eyJhdWQiOiIzYzAyYmFmZi0zY2Y2LTQ0ZWEtOTM4Yi05YjEwZjE3ODIxN2EiLCJleHAiOjE1ODEwODA2NjMsImlhdCI6MTU4MTA3NzA2MywiaXNzIjoibWVkaWFsYWIuYWZwLmNvbSIsInN1YiI6ImNlYWZmN2Y0LTc3NzMtNGQyNC04MWE1LTIyMTMwNWVlOTM2NyIsImF1dGhlbnRpY2F0aW9uVHlwZSI6IlBBU1NXT1JETEVTUyIsImVtYWlsIjoiZXJpYy5zY2hhZWZmZXJAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImFwcGxpY2F0aW9uSWQiOiIzYzAyYmFmZi0zY2Y2LTQ0ZWEtOTM4Yi05YjEwZjE3ODIxN2EiLCJyb2xlcyI6WyJ1c2VyIl19.q-4p_1SPaNWOGawFsszO9N_EDYRlpzijFlrWUgT2-EEJel6Kha4W9Zi6mlapTC5QXrAkN8RvGa89r6isJ24kDL5CleVMpRiN8LLr9DT8jUDcp1WBIVCqCzucTkMbGS-l7VOsmprkQM8-fDHY_rd32oNVZ5el0nhRtlv2FuF3bi-V2m7hg9m4qSrDUJe3ZXXYL7ulsZuAN9V6cfePJczicmdnxc1au7MMRPNYAD1SyKrrQjV33Pyp5Hko0ui9CY1wfFs1Gq8OywGjmlcXeGcHVIbAb-ZeJ_tfIjpINtoh8yUL2Xhd6jOY9Y7Wvb1BKdXIhUkyZwbclTGZz2D9BSjO_Q")
	public String token;
	public String refreshToken;
	public JwtUser user;

	/**
	 * Constructor.
	 */
	public JwtLoginResponse() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JwtLoginResponse other = (JwtLoginResponse) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (refreshToken == null) {
			if (other.refreshToken != null)
				return false;
		} else if (!refreshToken.equals(other.refreshToken))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JwtLoginResponse [token=" + token + ", user=" + user + ", refreshToken=\" + refreshToken + \"]";
	}
}
