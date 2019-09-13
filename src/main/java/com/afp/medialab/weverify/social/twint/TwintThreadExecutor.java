package com.afp.medialab.weverify.social.twint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
@EnableAsync
public class TwintThreadExecutor {

   @Bean(name = "twintCallTaskExecutor")
   public TaskExecutor twintCallTaskExecutor() {

      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(4);
      executor.setMaxPoolSize(4);
      executor.setThreadNamePrefix("twint-");
      executor.initialize();
      return executor;

   }
}
