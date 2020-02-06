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
public class JwtLoginRequest implements Serializable {
	private static final long serialVersionUID = 7160240555931206871L;

	@NotBlank(message = "Code is required")
	public String code;

	/**
	 * Constructor.
	 */
	public JwtLoginRequest() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
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
		JwtLoginRequest other = (JwtLoginRequest) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JwtLoginRequest [code=" + code + "]";
	}
}
