package com.afp.medialab.weverify.social.twint;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ESConfiguration extends AbstractElasticsearchConfiguration {

	private static Logger Logger = LoggerFactory.getLogger(ESConfiguration.class);

	@Value("${application.elasticsearch.host}")
	private String esHost;

	@Value("${application.elasticsearch.port}")
	private String esPort;

	@Value("${application.elasticsearch.url}")
	private String esURL;
	
//	@Value("${application.elasticsearch.cluster}")
//	private String esClusterName;

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Override
	public RestHighLevelClient elasticsearchClient() {
		RestHighLevelClient client = null;
		Logger.debug("host:" + esHost + "port:" + esPort);
		try {
			client = new RestHighLevelClient(
					RestClient.builder(new HttpHost(InetAddress.getByName(esHost), Integer.valueOf(esPort))));
		} catch (UnknownHostException e) {
			Logger.error("unknow host: " + esHost);
			throw new IllegalArgumentException("unknow host", e);
		}
		return client;
	}

}
