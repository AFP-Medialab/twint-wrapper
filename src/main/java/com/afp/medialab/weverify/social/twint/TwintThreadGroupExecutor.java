package com.afp.medialab.weverify.social.twint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TwintThreadGroupExecutor {

    @Value("${application.twintcall.nb-core-threads}")
    private int nbCoreThreads;
    @Value("${application.twintcall.nb-max-threads}")
    private  int nbMaxThreads;

    @Bean(name = "twintCallGroutTaskExecutor")
    public TaskExecutor twintCallGroutTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(nbCoreThreads);
        executor.setMaxPoolSize(nbMaxThreads);
        executor.setThreadNamePrefix("twintGroup-");
        executor.initialize();
        return executor;
    }
}