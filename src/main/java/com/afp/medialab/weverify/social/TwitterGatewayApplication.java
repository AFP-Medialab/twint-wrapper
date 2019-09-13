package com.afp.medialab.weverify.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.afp.medialab.weverify.social.dao.repository")
@SpringBootApplication
public class TwitterGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwitterGatewayApplication.class, args);
	}
}
