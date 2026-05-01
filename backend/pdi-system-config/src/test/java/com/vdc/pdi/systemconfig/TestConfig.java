package com.vdc.pdi.systemconfig;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * 测试配置类 - 用于 WebMvc 测试
 * 不包含 JPA 和数据库配置，避免数据库依赖
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(
    basePackages = {"com.vdc.pdi.systemconfig", "com.vdc.pdi.common"},
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                com.vdc.pdi.common.config.QuerydslConfig.class,
                com.vdc.pdi.common.config.JpaAuditingConfig.class
            }
        )
    }
)
@EnableMethodSecurity(prePostEnabled = true)
public class TestConfig {
}
