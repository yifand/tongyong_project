package com.vdc.pdi.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL配置类
 * 提供JPAQueryFactory Bean用于类型安全的复杂查询
 */
@Configuration
public class QuerydslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 创建JPAQueryFactory Bean
     * 用于构建QueryDSL查询
     *
     * @return JPAQueryFactory实例
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
