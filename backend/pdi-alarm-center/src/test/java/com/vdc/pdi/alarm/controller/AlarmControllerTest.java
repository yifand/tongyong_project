package com.vdc.pdi.alarm.controller;

import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.dto.response.AlarmStatisticsResponse;
import com.vdc.pdi.alarm.service.AlarmSSEService;
import com.vdc.pdi.alarm.service.AlarmService;
import com.vdc.pdi.common.dto.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 报警控制器测试
 */
@WebMvcTest(AlarmController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlarmService alarmService;

    @MockBean
    private AlarmSSEService alarmSSEService;

    @Test
    @DisplayName("应返回实时报警列表")
    void shouldReturnRealtimeAlarms() throws Exception {
        // Given
        when(alarmService.getRealtimeAlarms(50, null))
                .thenReturn(List.of(new AlarmResponse()));

        // When & Then
        mockMvc.perform(get("/api/v1/alarms/realtime")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("应建立SSE连接")
    void shouldEstablishSSEConnection() throws Exception {
        // Given
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter =
                new org.springframework.web.servlet.mvc.method.annotation.SseEmitter();
        when(alarmSSEService.subscribe()).thenReturn(emitter);

        // When & Then
        mockMvc.perform(get("/api/v1/alarms/sse"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    @Test
    @DisplayName("应支持历史报警分页查询")
    void shouldSupportHistoryPagination() throws Exception {
        // Given
        when(alarmService.getHistoryAlarms(any()))
                .thenReturn(PageResult.of(Collections.emptyList(), 0L, 1, 20));

        // When & Then
        mockMvc.perform(get("/api/v1/alarms/history")
                        .param("page", "1")
                        .param("size", "20")
                        .param("siteId", "1")
                        .param("type", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("应成功处理报警")
    void shouldProcessAlarm() throws Exception {
        // Given
        doNothing().when(alarmService).processAlarm(eq(1L), any());

        // When & Then
        mockMvc.perform(patch("/api/v1/alarms/1/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remark\": \"已处理\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("应成功标记误报")
    void shouldMarkFalsePositive() throws Exception {
        // Given
        doNothing().when(alarmService).markFalsePositive(eq(1L), any());

        // When & Then
        mockMvc.perform(patch("/api/v1/alarms/1/false-positive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remark\": \"误报\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("应返回今日统计")
    void shouldReturnTodayStatistics() throws Exception {
        // Given
        AlarmStatisticsResponse stats = new AlarmStatisticsResponse();
        stats.setTotal(25L);
        stats.setUnprocessed(3L);
        stats.setProcessed(22L);
        when(alarmService.getTodayStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/alarms/statistics/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(25))
                .andExpect(jsonPath("$.data.unprocessed").value(3))
                .andExpect(jsonPath("$.data.processed").value(22));
    }
}
