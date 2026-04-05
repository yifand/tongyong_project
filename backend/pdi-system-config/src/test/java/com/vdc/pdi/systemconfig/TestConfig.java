package com.vdc.pdi.systemconfig;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * 测试配置类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.vdc.pdi.systemconfig")
@EnableMethodSecurity(prePostEnabled = true)
public class TestConfig {
}
