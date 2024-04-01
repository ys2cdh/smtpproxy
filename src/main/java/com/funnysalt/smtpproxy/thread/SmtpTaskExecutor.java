package com.funnysalt.smtpproxy.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class SmtpTaskExecutor {

    @Bean(name = "executor")
    public Executor smtpTaskExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);	// 기본 스레드 수
        taskExecutor.setMaxPoolSize(10);	// 최대 스레드 수
        //taskExecutor.setQueueCapacity(100);	// Queue 사이즈
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(60);	// shutdown 최대 60초 대기
        return taskExecutor;
    }
}
