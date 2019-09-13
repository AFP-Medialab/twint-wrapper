package com.afp.medialab.weverify.social;

import com.afp.medialab.weverify.social.twint.TwintThreadExecutor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.dao.service.CollectService;

@SpringBootConfiguration
@ComponentScan(value = {"com.afp.medialab.weverify.social"})
public class EndpointMockConfiguration {


	@MockBean
	public TwintThreadExecutor twintCall;
	
	@MockBean
	public CollectService collectService;
	
	@MockBean
	public CollectInterface collectInterface;
}
