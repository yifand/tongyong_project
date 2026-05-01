package com.vdc.pdi.logmanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 日志清理定时任务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogCleanupService {

    private final OperationLogService operationLogService;
    private final SystemLogService systemLogService;

    /**
     * 每天凌晨2点执行日志清理
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupLogs() {
        log.info("开始执行日志清理任务");

        try {
            // 清理180天前的操作日志
            LocalDateTime operationLogExpireTime = LocalDateTime.now().minusDays(180);
            int operationDeleted = operationLogService.cleanupExpiredLogs(operationLogExpireTime);
            log.info("清理操作日志完成，删除 {} 条记录", operationDeleted);

            // 清理30天前的系统日志
            LocalDateTime systemLogExpireTime = LocalDateTime.now().minusDays(30);
            int systemDeleted = systemLogService.cleanupExpiredLogs(systemLogExpireTime);
            log.info("清理系统日志完成，删除 {} 条记录", systemDeleted);

        } catch (Exception e) {
            log.error("日志清理任务执行失败", e);
        }
    }
}
