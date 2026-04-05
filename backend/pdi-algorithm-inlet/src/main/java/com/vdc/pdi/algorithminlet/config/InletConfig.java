package com.vdc.pdi.algorithminlet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 算法数据入口模块配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "algorithm-inlet")
public class InletConfig {

    /**
     * 幂等配置
     */
    private IdempotencyConfig idempotency = new IdempotencyConfig();

    /**
     * 认证配置
     */
    private AuthConfig auth = new AuthConfig();

    @Data
    public static class IdempotencyConfig {
        /**
         * 幂等窗口期（分钟），在此时间内重复数据会被忽略
         */
        private int windowMinutes = 5;

        /**
         * 幂等记录过期时间（分钟）
         */
        private int expireMinutes = 10;
    }

    @Data
    public static class AuthConfig {
        /**
         * 认证缓存过期时间（分钟）
         */
        private int cacheExpireMinutes = 30;

        /**
         * Token密钥
         */
        private String secretKey = "default-secret-key";
    }
}
