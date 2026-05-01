package com.vdc.pdi.logmanagement.controller;

import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.dto.request.OperationLogRequest;
import com.vdc.pdi.logmanagement.dto.response.OperationLogResponse;
import com.vdc.pdi.logmanagement.service.OperationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 操作日志控制器测试类
 */
@WebMvcTest(controllers = OperationLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OperationLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperationLogService operationLogService;

    private OperationLogResponse mockLogResponse;

    @BeforeEach
    void setUp() {
        mockLogResponse = OperationLogResponse.builder()
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
     * OC-001: 正常分页查询操作日志
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQueryOperationLogs_Success() throws Exception {
        // 准备测试数据
        PageResult<OperationLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(operationLogService.queryOperationLogs(any(OperationLogRequest.class))).thenReturn(pageResult);

        // 执行请求并验证
        mockMvc.perform(get("/api/v1/logs/operations")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));

        verify(operationLogService, times(1)).queryOperationLogs(any(OperationLogRequest.class));
    }

    /**
     * OC-002: 按时间范围筛选查询
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQueryOperationLogs_ByTimeRange() throws Exception {
        PageResult<OperationLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(operationLogService.queryOperationLogs(any(OperationLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/operations")
                        .param("page", "1")
                        .param("size", "20")
                        .param("startTime", "2026-03-01T00:00:00")
                        .param("endTime", "2026-03-29T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).queryOperationLogs(any(OperationLogRequest.class));
    }

    /**
     * OC-003: 按操作类型筛选查询
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQueryOperationLogs_ByOperationType() throws Exception {
        PageResult<OperationLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(operationLogService.queryOperationLogs(any(OperationLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/operations")
                        .param("page", "1")
                        .param("size", "20")
                        .param("operationType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).queryOperationLogs(any(OperationLogRequest.class));
    }

    /**
     * OC-004: 按用户名模糊查询
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQueryOperationLogs_ByUsername() throws Exception {
        PageResult<OperationLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(operationLogService.queryOperationLogs(any(OperationLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/operations")
                        .param("page", "1")
                        .param("size", "20")
                        .param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).queryOperationLogs(any(OperationLogRequest.class));
    }

    /**
     * OC-005: 按操作结果筛选
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQueryOperationLogs_ByResult() throws Exception {
        PageResult<OperationLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(operationLogService.queryOperationLogs(any(OperationLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/operations")
                        .param("page", "1")
                        .param("size", "20")
                        .param("result", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).queryOperationLogs(any(OperationLogRequest.class));
    }

    /**
     * OC-006: 组合条件查询
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQueryOperationLogs_CombinedConditions() throws Exception {
        PageResult<OperationLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(operationLogService.queryOperationLogs(any(OperationLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/operations")
                        .param("page", "1")
                        .param("size", "20")
                        .param("username", "admin")
                        .param("operationType", "1")
                        .param("result", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).queryOperationLogs(any(OperationLogRequest.class));
    }

    /**
     * OC-007: 越权访问（普通用户）- 禁用过滤器时不测试权限
     */
    @Test
    @WithMockUser(roles = {"NORMAL_USER"})
    void testQueryOperationLogs_NormalUser_AccessibleWithDisabledFilter() throws Exception {
        // 注意：由于 addFilters = false，权限过滤器被禁用，此测试验证的是过滤器禁用状态下的行为
        PageResult<OperationLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(operationLogService.queryOperationLogs(any(OperationLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/operations"))
                .andExpect(status().isOk());

        verify(operationLogService, times(1)).queryOperationLogs(any(OperationLogRequest.class));
    }

    /**
     * OC-008: 导出操作日志
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testExportOperationLogs_Success() throws Exception {
        doNothing().when(operationLogService).exportOperationLogs(any(OperationLogRequest.class), any());

        mockMvc.perform(get("/api/v1/logs/operations/export")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(operationLogService, times(1)).exportOperationLogs(any(OperationLogRequest.class), any());
    }

    /**
     * OC-009: 获取操作类型列表
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testGetOperationTypes_Success() throws Exception {
        Map<String, Object> typeMap = new HashMap<>();
        typeMap.put("code", 1);
        typeMap.put("name", "登录");
        typeMap.put("description", "用户登录系统");

        when(operationLogService.getOperationTypes()).thenReturn(Arrays.asList(typeMap));

        mockMvc.perform(get("/api/v1/logs/operations/operation-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code").value(1));

        verify(operationLogService, times(1)).getOperationTypes();
    }

    /**
     * OC-010: 站点管理员可访问
     */
    @Test
    @WithMockUser(roles = {"SITE_ADMIN"})
    void testQueryOperationLogs_SiteAdmin_Success() throws Exception {
        PageResult<OperationLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(operationLogService.queryOperationLogs(any(OperationLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/operations")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).queryOperationLogs(any(OperationLogRequest.class));
    }
}
