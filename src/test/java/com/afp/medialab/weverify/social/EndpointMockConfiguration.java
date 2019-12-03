package com.afp.medialab.weverify.social;

import org.springframework.boot.test.mock.mockito.MockBean;

import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.twint.TwintThreadExecutor;

//@SpringBootConfiguration
//@ComponentScan(value = {"com.afp.medialab.weverify.social"})
public class EndpointMockConfiguration {


	@MockBean
	public TwintThreadExecutor twintCall;
	
	@MockBean
	public CollectService collectService;
	
	@MockBean
	public CollectInterface collectInterface;
}
