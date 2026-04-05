package com.vdc.pdi.behaviorarchive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 测试用Spring Boot启动类
 */
@SpringBootApplication(scanBasePackages = {"com.vdc.pdi.behaviorarchive", "com.vdc.pdi.common"})
@EntityScan(basePackages = {"com.vdc.pdi.behaviorarchive.domain.entity", "com.vdc.pdi.common.entity"})
@EnableJpaRepositories(basePackages = {"com.vdc.pdi.behaviorarchive.domain.repository"})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
