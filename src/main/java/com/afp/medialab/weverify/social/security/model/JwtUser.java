/**
 * 
 */
package com.afp.medialab.weverify.social.security.model;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JwtUser implements Serializable {
	private static final long serialVersionUID = -8694660337222202689L;

	@ApiModelProperty(value = "User's identifier.", required = true, example = "1b941adc-8e55-4f40-801d-614fd8a6318d")
	public UUID id;
	@ApiModelProperty(value = "User's email addresse and login identifier.", required = true,
			example = "email@address.com")
	public String email;
	@ApiModelProperty(value = "True if user account is activated.", example = "true")
	public boolean active;
	@ApiModelProperty(value = "User account expiration date.", example = "true")
	public ZonedDateTime expiry;
	@ApiModelProperty(value = "User's first name.", example = "John")
	public String firstName;
	@ApiModelProperty(value = "User's last name.", example = "SMITH")
	public String lastName;
	@ApiModelProperty(value = "User's company name.", example = "RICK SPIRIT")
	public String company;
	@ApiModelProperty(value = "User's position within company.", example = "FAKE NEWS CHECKER")
	public String position;
	@ApiModelProperty(value = "User's preferred languages.", example = "[ \"fr\", \"en\" ]")
	public List<Locale> preferredLanguages;
	@ApiModelProperty(value = "User's timezone ID.", example = "Europe/Paris")
	public ZoneId timezone;
	// public List<AuthUserRegistration> registrations;
	// public String securityRole;

	/**
	 * Constructor.
	 */
	public JwtUser() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((company == null) ? 0 : company.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((expiry == null) ? 0 : expiry.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		JwtUser other = (JwtUser) obj;
		if (active != other.active)
			return false;
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
		if (expiry == null) {
			if (other.expiry != null)
				return false;
		} else if (!expiry.equals(other.expiry))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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
		return "JwtUser [id=" + id + ", email=" + email + ", active=" + active + ", expiry=" + expiry + ", firstName="
				+ firstName + ", lastName=" + lastName + ", company=" + company + ", position=" + position
				+ ", preferredLanguages=" + preferredLanguages + ", timezone=" + timezone + "]";
	}
}
