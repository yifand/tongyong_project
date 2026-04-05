package com.vdc.pdi.device.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.device.dto.request.ChannelQueryRequest;
import com.vdc.pdi.device.dto.request.ChannelRequest;
import com.vdc.pdi.device.dto.response.ChannelResponse;
import com.vdc.pdi.device.service.ChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 通道控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ChannelService channelService;

    @BeforeEach
    void setUp() {
        ChannelController controller = new ChannelController(channelService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    void listChannels_Success() throws Exception {
        // Given
        ChannelResponse channelResponse = createChannelResponse();
        PageResponse<ChannelResponse> pageResponse = PageResponse.of(
                Arrays.asList(channelResponse), 1L, 1, 10);

        when(channelService.listChannels(any(ChannelQueryRequest.class), any())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/channels")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getChannel_Success() throws Exception {
        // Given
        ChannelResponse response = createChannelResponse();
        when(channelService.getChannel(eq(1L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/channels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试通道"));
    }

    @Test
    void createChannel_Success() throws Exception {
        // Given
        ChannelRequest request = new ChannelRequest();
        request.setBoxId(1L);
        request.setSiteId(1L);
        request.setName("测试通道");
        request.setType(0);
        request.setAlgorithmType("smoke");

        when(channelService.createChannel(any(ChannelRequest.class), any(), any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/devices/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void updateChannel_Success() throws Exception {
        // Given
        ChannelRequest request = new ChannelRequest();
        request.setBoxId(1L);
        request.setSiteId(1L);
        request.setName("更新后的通道");
        request.setType(0);

        // When & Then
        mockMvc.perform(put("/api/v1/devices/channels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteChannel_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/devices/channels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getChannelsByBoxId_Success() throws Exception {
        // Given
        List<ChannelResponse> channels = Arrays.asList(createChannelResponse());
        when(channelService.getChannelsByBoxId(eq(1L), any())).thenReturn(channels);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/channels/box/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("测试通道"));
    }

    private ChannelResponse createChannelResponse() {
        ChannelResponse response = new ChannelResponse();
        response.setId(1L);
        response.setBoxId(1L);
        response.setName("测试通道");
        response.setType(0);
        response.setStatus(1);
        response.setAlgorithmType("smoke");
        return response;
    }
}
