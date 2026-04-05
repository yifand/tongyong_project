package com.vdc.pdi.device.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.device.dto.request.BoxQueryRequest;
import com.vdc.pdi.device.dto.request.BoxRequest;
import com.vdc.pdi.device.dto.response.BoxResponse;
import com.vdc.pdi.device.dto.response.DeviceMetricsResponse;
import com.vdc.pdi.device.service.BoxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 盒子控制器测试
 */
@ExtendWith(MockitoExtension.class)
class BoxControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BoxService boxService;

    @BeforeEach
    void setUp() {
        BoxController controller = new BoxController(boxService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    void listBoxes_Success() throws Exception {
        // Given
        BoxResponse boxResponse = createBoxResponse();
        PageResponse<BoxResponse> pageResponse = PageResponse.of(
                Arrays.asList(boxResponse), 1L, 1, 10);

        when(boxService.listBoxes(any(BoxQueryRequest.class), any())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/boxes")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getBox_Success() throws Exception {
        // Given
        BoxResponse response = createBoxResponse();
        when(boxService.getBox(eq(1L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/boxes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试盒子"));
    }

    @Test
    void createBox_Success() throws Exception {
        // Given
        BoxRequest request = new BoxRequest();
        request.setName("测试盒子");
        request.setIpAddress("192.168.1.100");
        request.setSiteId(1L);

        when(boxService.createBox(any(BoxRequest.class), any(), any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/devices/boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void createBox_ValidationFailed() throws Exception {
        // Given - 缺少必填字段
        BoxRequest request = new BoxRequest();
        request.setName(""); // 空名称

        // When & Then
        mockMvc.perform(post("/api/v1/devices/boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBox_Success() throws Exception {
        // Given
        BoxRequest request = new BoxRequest();
        request.setName("更新后的盒子");
        request.setIpAddress("192.168.1.101");
        request.setSiteId(1L);

        // When & Then
        mockMvc.perform(put("/api/v1/devices/boxes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteBox_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/devices/boxes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void rebootBox_Success() throws Exception {
        // Given
        doNothing().when(boxService).rebootBox(eq(1L), any());

        // When & Then
        mockMvc.perform(post("/api/v1/devices/boxes/1/reboot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getBoxMetrics_Success() throws Exception {
        // Given
        DeviceMetricsResponse metrics = new DeviceMetricsResponse();
        metrics.setBoxId(1L);
        metrics.setBoxName("测试盒子");
        metrics.setCpuUsage(45.5);
        metrics.setMemoryUsage(60.0);
        metrics.setDiskUsage(70.0);

        when(boxService.getBoxMetrics(eq(1L), any())).thenReturn(metrics);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/boxes/1/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.cpuUsage").value(45.5));
    }

    private BoxResponse createBoxResponse() {
        BoxResponse response = new BoxResponse();
        response.setId(1L);
        response.setName("测试盒子");
        response.setIpAddress("192.168.1.100");
        response.setStatus(1);
        response.setSiteId(1L);
        return response;
    }
}
