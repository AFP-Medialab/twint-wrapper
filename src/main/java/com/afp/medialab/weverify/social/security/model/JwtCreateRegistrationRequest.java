/**
 * 
 */
package com.afp.medialab.weverify.social.security.model;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

/**
 * Register user payload object.
 * 
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JwtCreateRegistrationRequest implements Serializable {
	private static final long serialVersionUID = -2952101473443468656L;

	@NotBlank(message = "Email address is required")
	@Email(message = "Email address must be valid")
	@ApiModelProperty(value = "User's email addresse and login identifier.", required = true,
			example = "email@address.com")
	public String email;
	@ApiModelProperty(value = "User's first name.", required = true, example = "John")
	@NotBlank(message = "Firstname is required")
	public String firstName;
	@ApiModelProperty(value = "User's last name.", required = true, example = "SMITH")
	@NotBlank(message = "Lastname is required")
	public String lastName;
	@ApiModelProperty(value = "User's company name.", required = true, example = "RICK SPIRIT")
	public String company;
	@NotBlank(message = "Position is required")
	@ApiModelProperty(value = "User's position within company.", required = true, example = "FAKE NEWS CHECKER")
	public String position;
	@ApiModelProperty(value = "User's preferred languages.", required = false, example = "[ \"fr\", \"en\" ]")
	public List<Locale> preferredLanguages;
	@ApiModelProperty(value = "User's timezone ID.", required = false, example = "Europe/Paris")
	public ZoneId timezone;

	/**
	 * Constructor.
	 */
	public JwtCreateRegistrationRequest() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((company == null) ? 0 : company.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((preferredLanguages == null) ? 0 : preferredLanguages.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((timezone == null) ? 0 : timezone.hashCode());
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
		JwtCreateRegistrationRequest other = (JwtCreateRegistrationRequest) obj;
		if (company == null) {
			if (other.company != null)
				return false;
		} else if (!company.equals(other.company))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (preferredLanguages == null) {
			if (other.preferredLanguages != null)
				return false;
		} else if (!preferredLanguages.equals(other.preferredLanguages))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (timezone == null) {
			if (other.timezone != null)
				return false;
		} else if (!timezone.equals(other.timezone))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RegisterUserRequest [email=" + email + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", company=" + company + ", position=" + position + ", preferredLanguages=" + preferredLanguages
				+ ", timezone=" + timezone + "]";
	}
}
