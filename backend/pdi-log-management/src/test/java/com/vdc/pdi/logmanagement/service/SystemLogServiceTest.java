package com.vdc.pdi.logmanagement.service;

import com.querydsl.core.types.Predicate;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.domain.entity.SystemLog;
import com.vdc.pdi.logmanagement.domain.repository.SystemLogRepository;
import com.vdc.pdi.logmanagement.dto.request.SystemLogRequest;
import com.vdc.pdi.logmanagement.dto.response.SystemLogResponse;
import com.vdc.pdi.logmanagement.mapper.LogMapper;
import com.vdc.pdi.logmanagement.service.impl.SystemLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 系统日志服务测试类
 */
@ExtendWith(MockitoExtension.class)
class SystemLogServiceTest {

    @Mock
    private SystemLogRepository systemLogRepository;

    @Mock
    private LogMapper logMapper;

    @InjectMocks
    private SystemLogServiceImpl systemLogService;

    private SystemLog mockSystemLog;
    private SystemLogResponse mockSystemLogResponse;

    @BeforeEach
    void setUp() {
        mockSystemLog = SystemLog.builder()
                .id(1L)
                .level(3)
                .module("algorithm-inlet")
                .message("接收状态流数据失败：连接超时")
                .stackTrace("java.net.SocketTimeoutException: Read timed out")
                .sourceClass("com.vdc.pdi.algorithm.service.StateReceiver")
                .sourceMethod("receiveState")
                .threadName("http-nio-8080-exec-5")
                .createdAt(LocalDateTime.now())
                .build();

        mockSystemLogResponse = SystemLogResponse.builder()
                .id(1L)
                .level("ERROR")
                .levelCode(3)
                .module("algorithm-inlet")
                .message("接收状态流数据失败：连接超时")
                .stackTrace("java.net.SocketTimeoutException: Read timed out")
                .sourceClass("com.vdc.pdi.algorithm.service.StateReceiver")
                .sourceMethod("receiveState")
                .threadName("http-nio-8080-exec-5")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * SS-001: 保存系统日志
     */
    @Test
    void testSaveSystemLog() {
        when(systemLogRepository.save(any(SystemLog.class))).thenReturn(mockSystemLog);

        systemLogService.saveSystemLog(mockSystemLog);

        verify(systemLogRepository, times(1)).save(any(SystemLog.class));
    }

    /**
     * SS-002: 分页查询系统日志
     */
    @Test
    void testQuerySystemLogs_WithFilters() {
        SystemLogRequest request = new SystemLogRequest();
        request.setPage(1);
        request.setSize(20);
        request.setLevel(3);
        request.setModule("algorithm-inlet");
        request.setKeyword("超时");

        List<SystemLog> logs = Collections.singletonList(mockSystemLog);
        Page<SystemLog> page = new PageImpl<>(logs);

        when(systemLogRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);
        when(logMapper.toSystemLogResponseList(any())).thenReturn(Collections.singletonList(mockSystemLogResponse));

        PageResult<SystemLogResponse> result = systemLogService.querySystemLogs(request);

        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(1);
        verify(systemLogRepository, times(1)).findAll(any(com.querydsl.core.types.Predicate.class), any(Pageable.class));
    }

    /**
     * SS-003: 按日志级别筛选
     */
    @Test
    void testQuerySystemLogs_ByLevel() {
        SystemLogRequest request = new SystemLogRequest();
        request.setPage(1);
        request.setSize(20);
        request.setLevel(1); // INFO

        List<SystemLog> logs = Collections.singletonList(mockSystemLog);
        Page<SystemLog> page = new PageImpl<>(logs);

        when(systemLogRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);
        when(logMapper.toSystemLogResponseList(any())).thenReturn(Collections.singletonList(mockSystemLogResponse));

        PageResult<SystemLogResponse> result = systemLogService.querySystemLogs(request);

        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
    }

    /**
     * SS-004: 清理过期系统日志
     */
    @Test
    void testCleanupExpiredLogs() {
        LocalDateTime before = LocalDateTime.now().minusDays(30);
        when(systemLogRepository.deleteByCreatedAtBefore(before)).thenReturn(20);

        int deleted = systemLogService.cleanupExpiredLogs(before);

        assertThat(deleted).isEqualTo(20);
        verify(systemLogRepository, times(1)).deleteByCreatedAtBefore(before);
    }

    /**
     * SS-005: 获取日志级别列表
     */
    @Test
    void testGetLogLevels() {
        List<Map<String, Object>> levels = systemLogService.getLogLevels();

        assertThat(levels).isNotNull();
        assertThat(levels).hasSize(4); // DEBUG, INFO, WARN, ERROR

        Map<String, Object> firstLevel = levels.get(0);
        assertThat(firstLevel).containsKey("code");
        assertThat(firstLevel).containsKey("name");
        assertThat(firstLevel).containsKey("description");
    }

    /**
     * SS-006: 按时间范围筛选
     */
    @Test
    void testQuerySystemLogs_WithTimeRange() {
        SystemLogRequest request = new SystemLogRequest();
        request.setPage(1);
        request.setSize(20);
        request.setStartTime(LocalDateTime.now().minusDays(7));
        request.setEndTime(LocalDateTime.now());

        List<SystemLog> logs = Collections.singletonList(mockSystemLog);
        Page<SystemLog> page = new PageImpl<>(logs);

        when(systemLogRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);
        when(logMapper.toSystemLogResponseList(any())).thenReturn(Collections.singletonList(mockSystemLogResponse));

        PageResult<SystemLogResponse> result = systemLogService.querySystemLogs(request);

        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
    }

    /**
     * SS-007: 保存日志时异常处理
     */
    @Test
    void testSaveSystemLog_ExceptionHandling() {
        doThrow(new RuntimeException("Database error")).when(systemLogRepository).save(any(SystemLog.class));

        // 应该捕获异常，不抛出
        systemLogService.saveSystemLog(mockSystemLog);

        verify(systemLogRepository, times(1)).save(any(SystemLog.class));
    }
}
