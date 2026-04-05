package com.vdc.pdi.behaviorarchive.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 行为档案模块配置
 */
@Configuration
@ComponentScan(basePackages = "com.vdc.pdi.behaviorarchive")
@EnableJpaAuditing
public class BehaviorArchiveConfig {
}
