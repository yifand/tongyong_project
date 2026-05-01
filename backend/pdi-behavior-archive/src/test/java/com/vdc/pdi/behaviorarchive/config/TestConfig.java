package com.vdc.pdi.behaviorarchive.config;

import com.vdc.pdi.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Controller测试专用配置
 */
@Configuration
@Profile("test")
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "com.vdc.pdi.behaviorarchive.controller",
    "com.vdc.pdi.behaviorarchive.mapper"
})
@Import(GlobalExceptionHandler.class)
public class TestConfig {
}
