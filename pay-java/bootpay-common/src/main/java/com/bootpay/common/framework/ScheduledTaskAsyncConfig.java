package com.bootpay.common.framework;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 多先线程 执行定时器
 * @author Administrator
 *
 */
@Configuration
@EnableAsync
public class ScheduledTaskAsyncConfig {

   private int corePoolSize = 5;
   private int maxPoolSize =10;
   private int queueCapacity = 10;
   
   @Bean
   public Executor taskExecutor() {
       ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
       executor.setCorePoolSize(corePoolSize);
       executor.setMaxPoolSize(maxPoolSize);
       executor.setQueueCapacity(queueCapacity);
       executor.initialize();
       return executor;
   }


}
