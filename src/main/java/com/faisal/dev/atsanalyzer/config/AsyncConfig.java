package com.faisal.dev.atsanalyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class AsyncConfig {

    @Bean(name = "resumeTaskExecutor")
    public Executor resumeTaskExecutor() {

        ThreadPoolTaskExecutor taskExecutor =
                new ThreadPoolTaskExecutor();

        taskExecutor.setCorePoolSize(2);
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix(
                "resume-worker-"
        );
        taskExecutor.setWaitForTasksToCompleteOnShutdown(
                true
        );
        taskExecutor.setAwaitTerminationSeconds(30);
        taskExecutor.initialize();

        log.info("Configured async resume task executor");

        return taskExecutor;
    }
}
