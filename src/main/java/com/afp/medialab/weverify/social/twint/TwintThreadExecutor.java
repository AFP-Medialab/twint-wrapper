package com.afp.medialab.weverify.social.twint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
@EnableAsync
public class TwintThreadExecutor {

   @Value("${application.twintcall.nb-core-threads}")
   private int nbCoreThreads;
   @Value("${application.twintcall.nb-max-threads}")
   private int nbMaxThreads;
   @Value("${application.twintcall.nb-queue-threads}")
   private int nbQueueThreads; 
   
   @Value("${application.twintcall.grp-nb-core-threads}")
   private int nbCoreGroupThreads;
   @Value("${application.twintcall.grp-nb-max-threads}")
   private  int nbMaxGroupThreads;

   @Bean(name = "twintCallTaskExecutor")
   public TaskExecutor twintCallTaskExecutor() {

      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(nbCoreThreads);
      executor.setMaxPoolSize(nbMaxThreads);
      executor.setQueueCapacity(nbQueueThreads);
      executor.setThreadNamePrefix("twint-");
      executor.initialize();
      return executor;
   }
   
   @Bean(name = "twintCallGroupTaskExecutor")
   public TaskExecutor twintCallGroutTaskExecutor() {

       ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
       executor.setCorePoolSize(nbCoreGroupThreads);
       executor.setMaxPoolSize(nbMaxGroupThreads);
       executor.setThreadNamePrefix("twintGroup-");
       executor.initialize();
       return executor;
   }
}
