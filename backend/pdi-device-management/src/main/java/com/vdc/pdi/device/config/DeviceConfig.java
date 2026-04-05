package com.vdc.pdi.device.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 设备管理配置
 */
@Configuration
@ConfigurationProperties(prefix = "pdi.device")
@Data
public class DeviceConfig {

    /**
     * 心跳超时时间（秒），默认60秒
     */
    private Integer heartbeatTimeout = 60;

    /**
     * 是否启用自动离线检测
     */
    private Boolean enableAutoOffline = true;

    /**
     * 资源历史数据保留天数
     */
    private Integer metricsHistoryDays = 7;
}
