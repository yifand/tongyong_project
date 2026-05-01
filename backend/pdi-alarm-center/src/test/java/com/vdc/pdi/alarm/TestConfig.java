package com.vdc.pdi.alarm;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 测试配置类
 * 为单元测试提供Spring配置（非SpringBootApplication避免被WebMvcTest扫描）
 */
@Configuration
@ComponentScan(
    basePackages = "com.vdc.pdi.alarm",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Controller.*")
    }
)
@EntityScan("com.vdc.pdi.alarm.domain.entity")
@EnableJpaRepositories("com.vdc.pdi.alarm.domain.repository")
@EnableJpaAuditing
public class TestConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 配置JPAQueryFactory用于QueryDSL测试
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
