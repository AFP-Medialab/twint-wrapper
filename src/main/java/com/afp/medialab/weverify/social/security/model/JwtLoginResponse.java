/**
 * 
 */
package com.afp.medialab.weverify.social.security.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JwtLoginResponse implements Serializable {
	private static final long serialVersionUID = -6669175977712645213L;

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
		result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		if (refreshToken == null) {
			if (other.refreshToken != null)
				return false;
		} else if (!refreshToken.equals(other.refreshToken))
			return false;
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
		return true;
	}

	@Override
	public String toString() {
		return "JwtLoginResponse [token=" + token + ", refreshToken=" + refreshToken + ", user=" + user + "]";
	}
}
