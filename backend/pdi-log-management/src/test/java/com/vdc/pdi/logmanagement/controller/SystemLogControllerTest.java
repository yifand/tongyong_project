package com.vdc.pdi.logmanagement.controller;

import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.dto.request.SystemLogRequest;
import com.vdc.pdi.logmanagement.dto.response.SystemLogResponse;
import com.vdc.pdi.logmanagement.service.SystemLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 系统日志控制器测试类
 */
@WebMvcTest(controllers = SystemLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SystemLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemLogService systemLogService;

    private SystemLogResponse mockLogResponse;

    @BeforeEach
    void setUp() {
        mockLogResponse = SystemLogResponse.builder()
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
     * SC-001: 正常分页查询系统日志
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQuerySystemLogs_Success() throws Exception {
        // 准备测试数据
        PageResult<SystemLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(systemLogService.querySystemLogs(any(SystemLogRequest.class))).thenReturn(pageResult);

        // 执行请求并验证
        mockMvc.perform(get("/api/v1/logs/system")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));

        verify(systemLogService, times(1)).querySystemLogs(any(SystemLogRequest.class));
    }

    /**
     * SC-002: 按日志级别筛选
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQuerySystemLogs_ByLevel() throws Exception {
        PageResult<SystemLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(systemLogService.querySystemLogs(any(SystemLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/system")
                        .param("page", "1")
                        .param("size", "20")
                        .param("level", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(systemLogService, times(1)).querySystemLogs(any(SystemLogRequest.class));
    }

    /**
     * SC-003: 按模块名称筛选
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQuerySystemLogs_ByModule() throws Exception {
        PageResult<SystemLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(systemLogService.querySystemLogs(any(SystemLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/system")
                        .param("page", "1")
                        .param("size", "20")
                        .param("module", "algorithm-inlet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(systemLogService, times(1)).querySystemLogs(any(SystemLogRequest.class));
    }

    /**
     * SC-004: 关键字搜索
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testQuerySystemLogs_ByKeyword() throws Exception {
        PageResult<SystemLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(systemLogService.querySystemLogs(any(SystemLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/system")
                        .param("page", "1")
                        .param("size", "20")
                        .param("keyword", "超时"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(systemLogService, times(1)).querySystemLogs(any(SystemLogRequest.class));
    }

    /**
     * SC-005: 站点管理员访问 - 禁用过滤器时测试
     */
    @Test
    @WithMockUser(roles = {"SITE_ADMIN"})
    void testQuerySystemLogs_SiteAdmin_AccessibleWithDisabledFilter() throws Exception {
        // 注意：由于 addFilters = false，权限过滤器被禁用，此测试验证的是过滤器禁用状态下的行为
        PageResult<SystemLogResponse> pageResult = PageResult.of(
                Collections.singletonList(mockLogResponse),
                1L, 1, 20
        );
        when(systemLogService.querySystemLogs(any(SystemLogRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/logs/system"))
                .andExpect(status().isOk());

        verify(systemLogService, times(1)).querySystemLogs(any(SystemLogRequest.class));
    }

    /**
     * SC-006: 获取日志级别列表
     */
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testGetLogLevels_Success() throws Exception {
        Map<String, Object> levelMap = new HashMap<>();
        levelMap.put("code", 1);
        levelMap.put("name", "INFO");
        levelMap.put("description", "普通信息");

        when(systemLogService.getLogLevels()).thenReturn(Collections.singletonList(levelMap));

        mockMvc.perform(get("/api/v1/logs/system/levels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code").value(1));

        verify(systemLogService, times(1)).getLogLevels();
    }
}
