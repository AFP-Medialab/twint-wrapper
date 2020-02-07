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
public class JwtRefreshTokenResponse implements Serializable {
	private static final long serialVersionUID = 1755310917798358684L;

	public String token;

	/**
	 * Constructor.
	 */
	public JwtRefreshTokenResponse() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
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
		JwtRefreshTokenResponse other = (JwtRefreshTokenResponse) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JwtRefreshTokenResponse [token=" + token + "]";
	}
}
