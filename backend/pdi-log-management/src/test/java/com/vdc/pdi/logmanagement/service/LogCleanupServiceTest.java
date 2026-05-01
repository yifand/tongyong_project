package com.vdc.pdi.logmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 日志清理服务测试类
 */
@ExtendWith(MockitoExtension.class)
class LogCleanupServiceTest {

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private LogCleanupService logCleanupService;

    /**
     * LC-001: 正常执行日志清理
     */
    @Test
    void testCleanupLogs_Success() {
        // 模拟清理操作日志返回10条
        when(operationLogService.cleanupExpiredLogs(any(LocalDateTime.class))).thenReturn(10);
        // 模拟清理系统日志返回20条
        when(systemLogService.cleanupExpiredLogs(any(LocalDateTime.class))).thenReturn(20);

        // 执行清理
        logCleanupService.cleanupLogs();

        // 验证两个服务的方法都被调用
        verify(operationLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
        verify(systemLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
    }

    /**
     * LC-002: 无可清理数据
     */
    @Test
    void testCleanupLogs_NoDataToClean() {
        // 模拟无数据可清理
        when(operationLogService.cleanupExpiredLogs(any(LocalDateTime.class))).thenReturn(0);
        when(systemLogService.cleanupExpiredLogs(any(LocalDateTime.class))).thenReturn(0);

        // 执行清理
        logCleanupService.cleanupLogs();

        // 验证调用
        verify(operationLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
        verify(systemLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
    }

    /**
     * LC-003: 清理执行异常
     */
    @Test
    void testCleanupLogs_ExceptionHandling() {
        // 模拟操作日志清理抛出异常
        when(operationLogService.cleanupExpiredLogs(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // 执行清理 - 应该捕获异常，不抛出
        logCleanupService.cleanupLogs();

        // 验证操作日志清理被调用
        verify(operationLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
        // 系统日志清理不应该被调用（因为前面抛异常了）
        verify(systemLogService, never()).cleanupExpiredLogs(any(LocalDateTime.class));
    }

    /**
     * LC-004: 部分清理失败（操作日志成功，系统日志失败）
     */
    @Test
    void testCleanupLogs_PartialFailure() {
        // 操作日志清理成功
        when(operationLogService.cleanupExpiredLogs(any(LocalDateTime.class))).thenReturn(10);
        // 系统日志清理失败
        when(systemLogService.cleanupExpiredLogs(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("System log cleanup failed"));

        // 执行清理 - 应该捕获异常
        logCleanupService.cleanupLogs();

        // 验证两个方法都被调用
        verify(operationLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
        verify(systemLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
    }

    /**
     * LC-005: 验证清理时间范围
     */
    @Test
    void testCleanupLogs_VerifyTimeRange() {
        // 捕获传递给服务方法的参数
        when(operationLogService.cleanupExpiredLogs(any(LocalDateTime.class))).thenAnswer(invocation -> {
            LocalDateTime before = invocation.getArgument(0);
            // 验证时间范围大约是180天前
            LocalDateTime expectedTime = LocalDateTime.now().minusDays(180);
            assertThat(before).isNotNull();
            assertThat(before).isBeforeOrEqualTo(expectedTime);
            return 10;
        });

        when(systemLogService.cleanupExpiredLogs(any(LocalDateTime.class))).thenAnswer(invocation -> {
            LocalDateTime before = invocation.getArgument(0);
            // 验证时间范围大约是30天前
            LocalDateTime expectedTime = LocalDateTime.now().minusDays(30);
            assertThat(before).isNotNull();
            assertThat(before).isBeforeOrEqualTo(expectedTime);
            return 20;
        });

        // 执行清理
        logCleanupService.cleanupLogs();

        // 验证调用
        verify(operationLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
        verify(systemLogService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
    }
}
