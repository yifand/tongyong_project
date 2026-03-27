package com.pdi.receiver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 接收服务配置
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Configuration
@EnableScheduling
public class ReceiverConfig {

    // 调度任务配置在HeartbeatServiceImpl中通过@Scheduled注解实现

}
