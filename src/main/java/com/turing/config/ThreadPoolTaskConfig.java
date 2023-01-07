package com.turing.config;

import jodd.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2023年01月06日 22:27:57
 */
@Configuration
@EnableAsync
public class ThreadPoolTaskConfig {
    @Bean("cacheThreadPool")
    public ExecutorService getCacheThreadPool() {
        return Executors.newCachedThreadPool(ThreadFactoryBuilder.create()
                                                                 .setNameFormat("Fast-Task-Thread-Pool-%s")
                                                                 .get());
    }

    @Bean
    public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setKeepAliveSeconds(30);
        threadPoolTaskExecutor.setQueueCapacity(1024);
        threadPoolTaskExecutor.setThreadNamePrefix("Common-Thread-Pool-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return threadPoolTaskExecutor;
    }
}
