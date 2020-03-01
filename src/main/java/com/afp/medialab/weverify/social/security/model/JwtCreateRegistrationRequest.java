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
import javax.validation.constraints.NotNull;

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
	@ApiModelProperty(value = "User's organization name.", required = true, example = "RICK SPIRIT")
	@NotBlank(message = "Organization is required")
	public String organization;
	@NotNull(message = "Role is required")
	@ApiModelProperty(value = "User's role within organization.", required = true, example = "FAKE_NEWS_CHECKER")
	public JwtUserOrganizationRole organizationRole;
	@ApiModelProperty(value = "User's role within organization (if OTHER).", required = true, example = "Tester")
	public String organizationRoleOther;
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
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((organization == null) ? 0 : organization.hashCode());
		result = prime * result + ((organizationRole == null) ? 0 : organizationRole.hashCode());
		result = prime * result + ((organizationRoleOther == null) ? 0 : organizationRoleOther.hashCode());
		result = prime * result + ((preferredLanguages == null) ? 0 : preferredLanguages.hashCode());
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
		if (organization == null) {
			if (other.organization != null)
				return false;
		} else if (!organization.equals(other.organization))
			return false;
		if (organizationRole != other.organizationRole)
			return false;
		if (organizationRoleOther == null) {
			if (other.organizationRoleOther != null)
				return false;
		} else if (!organizationRoleOther.equals(other.organizationRoleOther))
			return false;
		if (preferredLanguages == null) {
			if (other.preferredLanguages != null)
				return false;
		} else if (!preferredLanguages.equals(other.preferredLanguages))
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
		return "JwtCreateRegistrationRequest [email=" + email + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", organization=" + organization + ", organizationRole=" + organizationRole
				+ ", organizationRoleOther=" + organizationRoleOther + ", preferredLanguages=" + preferredLanguages
				+ ", timezone=" + timezone + "]";
	}
}
