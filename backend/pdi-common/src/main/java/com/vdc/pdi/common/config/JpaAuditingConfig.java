package com.vdc.pdi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA审计配置
 * 启用自动填充创建时间、更新时间、创建人
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    /**
     * 当前操作人获取器
     * 从SecurityContext中获取当前登录用户ID
     */
    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            // 从SecurityContext获取当前用户ID
            // 实际实现需要结合SecurityUtils
            Long currentUserId = getCurrentUserIdFromSecurityContext();
            return Optional.ofNullable(currentUserId);
        };
    }

    private Long getCurrentUserIdFromSecurityContext() {
        // 实际实现需要获取SecurityContext中的用户ID
        // 这里返回null，表示系统操作或无用户上下文
        return null;
    }
}
