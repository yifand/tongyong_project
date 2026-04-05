package com.vdc.pdi.device;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * 测试配置类
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.vdc.pdi.device.domain.entity", "com.vdc.pdi.common.entity"})
@EnableJpaAuditing(auditorAwareRef = "testAuditorAware")
public class TestConfiguration {

    /**
     * 测试用审计用户提供者
     */
    @Bean
    public AuditorAware<Long> testAuditorAware() {
        return () -> Optional.of(1L);
    }
}
