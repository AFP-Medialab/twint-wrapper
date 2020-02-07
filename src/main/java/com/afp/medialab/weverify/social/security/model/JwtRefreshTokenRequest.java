/**
 * 
 */
package com.afp.medialab.weverify.social.security.model;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JwtRefreshTokenRequest implements Serializable {
	private static final long serialVersionUID = -124946698266454699L;

	@NotBlank(message = "Refresh token is required")
	public String refreshToken;

	/**
	 * Constructor.
	 */
	public JwtRefreshTokenRequest() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		JwtRefreshTokenRequest other = (JwtRefreshTokenRequest) obj;
		if (refreshToken == null) {
			if (other.refreshToken != null)
				return false;
		} else if (!refreshToken.equals(other.refreshToken))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JwtRefreshTokenRequest [refreshToken=" + refreshToken + "]";
	}
}
