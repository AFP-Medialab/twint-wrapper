package com.afp.medialab.weverify.social.twint;

import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ESConfiguration extends AbstractElasticsearchConfiguration {

	private static Logger Logger = LoggerFactory.getLogger(ESConfiguration.class);

	@Value("${application.elasticsearch.host}")
	private String esHost;

	@Value("${application.elasticsearch.port}")
	private String esPort;

	@Bean("ESRestTempate")
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Override
	public RestHighLevelClient elasticsearchClient() {

		Logger.info("host: {}, port: {}", esHost, esPort);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Host", esHost);
		String esURL = esHost + ":" + esPort;
		final ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(esURL)
				.withDefaultHeaders(httpHeaders).build();
		return RestClients.create(clientConfiguration).rest();
	}

}
