/**
 * 
 */
package com.afp.medialab.weverify.social.security.model;

import java.util.UUID;

import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;

/**
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
public class FusionAuthDataConverter {

	public static JwtUser toJwtUser(User user, UUID applicationId) {
		if (user == null) {
			return null;
		}

		JwtUser jwtUser = new JwtUser();

		jwtUser.id = user.id;
		jwtUser.email = user.email;
		jwtUser.active = user.active;
		jwtUser.expiry = user.expiry;
		jwtUser.firstName = user.firstName;
		jwtUser.lastName = user.lastName;
		if (!user.data.isEmpty()) {
			jwtUser.company = (String) user.data.get("company");
			jwtUser.position = (String) user.data.get("position");
		}
		UserRegistration userRegistration = (applicationId != null ? user.getRegistrationForApplication(applicationId)
				: null);
		jwtUser.preferredLanguages = userRegistration != null
				? userRegistration.preferredLanguages != null ? userRegistration.preferredLanguages
						: user.preferredLanguages
				: user.preferredLanguages;
		jwtUser.timezone = userRegistration != null
				? userRegistration.timezone != null ? userRegistration.timezone : user.timezone
				: user.timezone;
		// securityRole / applicationRole / authRole / registrationRole

		return jwtUser;
	}

	// No constructor.
	private FusionAuthDataConverter() {
		super();
	}
}
