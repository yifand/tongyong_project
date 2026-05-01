package com.vdc.pdi.logmanagement.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.domain.entity.OperationLog;
import com.vdc.pdi.logmanagement.domain.repository.OperationLogRepository;
import com.vdc.pdi.logmanagement.dto.request.OperationLogRequest;
import com.vdc.pdi.logmanagement.dto.response.OperationLogResponse;
import com.vdc.pdi.logmanagement.mapper.LogMapper;
import com.vdc.pdi.logmanagement.service.impl.OperationLogServiceImpl;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 操作日志服务测试类
 */
@ExtendWith(MockitoExtension.class)
class OperationLogServiceTest {

    @Mock
    private OperationLogRepository operationLogRepository;

    @Mock
    private LogMapper logMapper;

    @InjectMocks
    private OperationLogServiceImpl operationLogService;

    private OperationLog mockOperationLog;
    private OperationLogResponse mockOperationLogResponse;

    @BeforeEach
    void setUp() {
        mockOperationLog = new OperationLog();
        mockOperationLog.setSiteId(1L);
        mockOperationLog.setUserId(1L);
        mockOperationLog.setUsername("admin");
        mockOperationLog.setIpAddress("192.168.1.100");
        mockOperationLog.setOperationType(1);
        mockOperationLog.setOperationDetail("用户登录系统");
        mockOperationLog.setRequestParams("{\"username\":\"admin\"}");
        mockOperationLog.setResult(1);
        mockOperationLog.setErrorMsg(null);
        mockOperationLog.setExecutionTime(125L);
        mockOperationLog.setCreatedAt(LocalDateTime.now());

        mockOperationLogResponse = OperationLogResponse.builder()
                .id(1L)
                .userId(1L)
                .username("admin")
                .ipAddress("192.168.1.100")
                .operationType("登录")
                .operationTypeCode(1)
                .operationDetail("用户登录系统")
                .requestParams("{\"username\":\"admin\"}")
                .result(1)
                .errorMsg(null)
                .executionTime(125L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * OS-001: 保存操作日志
     */
    @Test
    void testSaveOperationLog() {
        when(operationLogRepository.save(any(OperationLog.class))).thenReturn(mockOperationLog);

        OperationLog saved = operationLogRepository.save(mockOperationLog);

        assertThat(saved).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("admin");
        verify(operationLogRepository, times(1)).save(any(OperationLog.class));
    }

    /**
     * OS-003: 查询操作日志（带筛选条件）
     */
    @Test
    void testQueryOperationLogs_WithFilters() {
        // 准备请求
        OperationLogRequest request = new OperationLogRequest();
        request.setPage(1);
        request.setSize(20);
        request.setUsername("admin");
        request.setOperationType(1);
        request.setResult(1);

        List<OperationLog> logs = Collections.singletonList(mockOperationLog);
        Page<OperationLog> page = new PageImpl<>(logs);

        when(operationLogRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);
        when(logMapper.toOperationLogResponseList(any())).thenReturn(Collections.singletonList(mockOperationLogResponse));

        // 执行查询
        PageResult<OperationLogResponse> result = operationLogService.queryOperationLogs(request);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(1);
        verify(operationLogRepository, times(1)).findAll(any(com.querydsl.core.types.Predicate.class), any(Pageable.class));
    }

    /**
     * OS-005: 清理过期操作日志
     */
    @Test
    void testCleanupExpiredLogs() {
        LocalDateTime before = LocalDateTime.now().minusDays(180);
        when(operationLogRepository.deleteByCreatedAtBefore(before)).thenReturn(10);

        int deleted = operationLogService.cleanupExpiredLogs(before);

        assertThat(deleted).isEqualTo(10);
        verify(operationLogRepository, times(1)).deleteByCreatedAtBefore(before);
    }

    /**
     * OS-006: 获取操作类型列表
     */
    @Test
    void testGetOperationTypes() {
        List<Map<String, Object>> types = operationLogService.getOperationTypes();

        assertThat(types).isNotNull();
        assertThat(types).hasSize(10); // 10种操作类型

        // 验证第一个类型
        Map<String, Object> firstType = types.get(0);
        assertThat(firstType).containsKey("code");
        assertThat(firstType).containsKey("name");
        assertThat(firstType).containsKey("description");
    }

    /**
     * OS-007: 按时间范围筛选查询
     */
    @Test
    void testQueryOperationLogs_WithTimeRange() {
        OperationLogRequest request = new OperationLogRequest();
        request.setPage(1);
        request.setSize(20);
        request.setStartTime(LocalDateTime.now().minusDays(7));
        request.setEndTime(LocalDateTime.now());

        List<OperationLog> logs = Collections.singletonList(mockOperationLog);
        Page<OperationLog> page = new PageImpl<>(logs);

        when(operationLogRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);
        when(logMapper.toOperationLogResponseList(any())).thenReturn(Collections.singletonList(mockOperationLogResponse));

        PageResult<OperationLogResponse> result = operationLogService.queryOperationLogs(request);

        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        verify(operationLogRepository, times(1)).findAll(any(com.querydsl.core.types.Predicate.class), any(Pageable.class));
    }

    /**
     * OS-008: 统计指定用户的操作次数
     */
    @Test
    void testCountByUserIdAndCreatedAtBetween() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        when(operationLogRepository.countByUserIdAndCreatedAtBetween(1L, start, end)).thenReturn(5L);

        long count = operationLogRepository.countByUserIdAndCreatedAtBetween(1L, start, end);

        assertThat(count).isEqualTo(5L);
        verify(operationLogRepository, times(1)).countByUserIdAndCreatedAtBetween(1L, start, end);
    }
}
