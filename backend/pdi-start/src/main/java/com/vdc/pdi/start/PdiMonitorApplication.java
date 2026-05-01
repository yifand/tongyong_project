package com.vdc.pdi.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PDI智能监测平台 - Spring Boot启动类
 *
 * @author PDI开发团队
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {
        "com.vdc.pdi.common",
        "com.vdc.pdi.auth",
        "com.vdc.pdi.device",
        "com.vdc.pdi.ruleengine",
        "com.vdc.pdi.systemconfig",
        "com.vdc.pdi.algorithminlet",
        "com.vdc.pdi.behaviorarchive",
        "com.vdc.pdi.alarm",
        "com.vdc.pdi.start"
})
public class PdiMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdiMonitorApplication.class, args);
    }

}
