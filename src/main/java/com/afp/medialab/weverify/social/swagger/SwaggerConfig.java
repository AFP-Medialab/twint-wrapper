package com.afp.medialab.weverify.social.swagger;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				// Do not include default response codes in documentation
				.useDefaultResponseMessages(false)
				// Select API to document
				.select().apis(
						// RequestHandlerSelectors.basePackage("com.afp.medialab.weverify.social.controller")
						RequestHandlerSelectors.basePackage("com.afp.medialab.weverify.social")
				// RequestHandlerSelectors.any()
				).paths(PathSelectors.any()).build().apiInfo(apiInfo());
	}

	private ApiInfo apiInfo() {
		return new ApiInfo("WeVerify Twitter Gateway", "Gateway that's wrap Twitter scraping tool", "Draft 1",
				"Terms of service", new Contact("AFP Medialab", "http://www.afp.com", "medialab@afp.com"),
				"License of API", "API license URL", Collections.emptyList());
	}
}