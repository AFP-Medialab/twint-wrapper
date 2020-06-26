package com.afp.medialab.weverify.social;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GeneratePassword {

	/**
	 * Use this utility to create encrypted password
	 * DO NOT COMMIT THIS CLASS WITH THE PRODUCTION PASSWORD
	 * @param args
	 */
	public static void main(String[] args) {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		 // outputs {bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
		 // remember the password that is printed out and use in the next step
		 System.out.println(encoder.encode(args[0]));
		 int result = 123233 / 5000;
		 System.out.println(result);

	}

}
