/**
 * 
 */
package com.afp.medialab.weverify.social.security.model;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JwtCreateAccessCodeRequest implements Serializable {
	private static final long serialVersionUID = 7559989713586303996L;

	@NotBlank(message = "Email address is required")
	@Email(message = "Email address must be valid")
	@ApiModelProperty(value = "User's email addresse and login identifier.", required = true,
			example = "eric.schaeffer@rickspirit.io")
	public String email;

	/**
	 * Constructor.
	 */
	public JwtCreateAccessCodeRequest() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
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
		JwtCreateAccessCodeRequest other = (JwtCreateAccessCodeRequest) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccessCodeRequest [email=" + email + "]";
	}
}
