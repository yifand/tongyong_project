package com.vdc.pdi.ruleengine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 规则引擎配置类
 */
@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class RuleEngineConfig {

    /**
     * 规则引擎异步执行器
     */
    @Bean("ruleEngineExecutor")
    public Executor ruleEngineExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("rule-engine-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("规则引擎线程池初始化完成: corePoolSize=4, maxPoolSize=16, queueCapacity=1000");
        return executor;
    }
}
