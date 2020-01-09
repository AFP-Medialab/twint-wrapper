package com.afp.medialab.weverify.social;

import com.afp.medialab.weverify.social.twint.ESOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.afp.medialab.weverify.social.dao.repository")
@SpringBootApplication
public class TwitterGatewayApplication {


	/*@Autowired
	private static ESOperations esOperation;*/

	public static void main(String[] args) {
	//	esOperation.upgradeRefreshInterval();
		SpringApplication.run(TwitterGatewayApplication.class, args);
	}
}
