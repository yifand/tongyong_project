package com.vdc.pdi.behaviorarchive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.behaviorarchive.dto.request.ArchiveListRequest;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveDetailResponse;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveResponse;
import com.vdc.pdi.behaviorarchive.enums.ArchiveStatus;
import com.vdc.pdi.behaviorarchive.exception.ArchiveException;
import com.vdc.pdi.behaviorarchive.service.ArchiveExportService;
import com.vdc.pdi.behaviorarchive.service.BehaviorArchiveService;
import com.vdc.pdi.common.dto.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 行为档案控制器测试
 * 使用@WebMvcTest进行Controller层单元测试
 */
@WebMvcTest(BehaviorArchiveController.class)
@ActiveProfiles("test")
@DisplayName("行为档案控制器测试")
class BehaviorArchiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BehaviorArchiveService archiveService;

    @MockBean
    private ArchiveExportService exportService;

    // ==================== 档案列表查询测试 ====================

    @Test
    @DisplayName("档案列表查询 - 正常流程")
    void listArchives_Success() throws Exception {
        // Given
        ArchiveResponse response1 = createArchiveResponse(1001L, 1, "金桥库");
        ArchiveResponse response2 = createArchiveResponse(1002L, 2, "凯迪库");
        PageResult<ArchiveResponse> result = PageResult.of(
                List.of(response1, response2), 2, 1, 20
        );

        when(archiveService.queryArchiveList(any(ArchiveListRequest.class), eq(1), eq(20)))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/v1/archives")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list", hasSize(2)))
                .andExpect(jsonPath("$.data.list[0].id").value(1001))
                .andExpect(jsonPath("$.data.list[0].status").value(1))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("档案列表查询 - 带筛选条件查询")
    void listArchives_WithFilters() throws Exception {
        // Given
        ArchiveResponse response = createArchiveResponse(1001L, 1, "金桥库");
        PageResult<ArchiveResponse> result = PageResult.of(
                List.of(response), 1, 1, 20
        );

        when(archiveService.queryArchiveList(any(ArchiveListRequest.class), eq(1), eq(20)))
                .thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/v1/archives")
                        .param("siteId", "1")
                        .param("status", "1")
                        .param("channelId", "101")
                        .param("startTimeFrom", "2026-03-01 00:00:00")
                        .param("startTimeTo", "2026-03-31 23:59:59")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list", hasSize(1)));
    }

    @Test
    @DisplayName("档案列表查询 - 参数校验：page小于1")
    void listArchives_PageLessThanOne() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/archives")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("档案列表查询 - 参数校验：size大于100")
    void listArchives_SizeGreaterThan100() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/archives")
                        .param("page", "1")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("档案列表查询 - 参数校验：size小于1")
    void listArchives_SizeLessThanOne() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/archives")
                        .param("page", "1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    // ==================== 档案详情查询测试 ====================

    @Test
    @DisplayName("档案详情查询 - 成功场景")
    void getArchiveDetail_Success() throws Exception {
        // Given
        ArchiveDetailResponse detailResponse = createArchiveDetailResponse(1001L, 1);

        when(archiveService.getArchiveDetail(1001L))
                .thenReturn(detailResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/archives/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1001))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.statusText").value("达标"))
                .andExpect(jsonPath("$.data.channel.id").value(101))
                .andExpect(jsonPath("$.data.site.id").value(1));
    }

    @Test
    @DisplayName("档案详情查询 - 档案不存在")
    void getArchiveDetail_NotFound() throws Exception {
        // Given
        when(archiveService.getArchiveDetail(999L))
                .thenThrow(new ArchiveException("档案不存在"));

        // When & Then
        mockMvc.perform(get("/api/v1/archives/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1101));
    }

    @Test
    @DisplayName("档案详情查询 - 无权访问")
    void getArchiveDetail_NoPermission() throws Exception {
        // Given
        when(archiveService.getArchiveDetail(1001L))
                .thenThrow(new ArchiveException(com.vdc.pdi.common.enums.ResultCode.PERMISSION_DENIED, "无权访问该档案"));

        // When & Then
        mockMvc.perform(get("/api/v1/archives/1001"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    // ==================== 图片包下载测试 ====================

    @Test
    @DisplayName("图片包下载 - 正常流程")
    void downloadImagePackage_Success() throws Exception {
        // Given
        StreamingResponseBody stream = out -> {
            out.write("ZIP_CONTENT".getBytes());
        };

        when(exportService.downloadImagePackage(eq(1001L), any()))
                .thenReturn(ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archive_1001.zip")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(stream));

        // When & Then
        mockMvc.perform(get("/api/v1/archives/1001/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archive_1001.zip"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    @DisplayName("图片包下载 - 档案不存在")
    void downloadImagePackage_NotFound() throws Exception {
        // Given
        when(exportService.downloadImagePackage(eq(999L), any()))
                .thenThrow(new ArchiveException("档案不存在"));

        // When & Then
        mockMvc.perform(get("/api/v1/archives/999/download"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1101));
    }

    // ==================== 辅助方法 ====================

    private ArchiveResponse createArchiveResponse(Long id, int status, String siteName) {
        ArchiveResponse response = new ArchiveResponse();
        response.setId(id);
        response.setPdiTaskId(id + 1000);
        response.setChannelId(101L);
        response.setChannelName("左前门通道");
        response.setSiteId(1L);
        response.setSiteName(siteName);
        response.setStartTime(LocalDateTime.of(2026, 3, 25, 10, 0, 0));
        response.setEndTime(LocalDateTime.of(2026, 3, 25, 10, 8, 32));
        response.setEstimatedDuration(10);
        response.setActualDuration(512);
        response.setStatus(status);
        response.setStatusText(ArchiveStatus.fromCode(status).getText());
        response.setCreatedAt(LocalDateTime.of(2026, 3, 25, 10, 0, 0));
        return response;
    }

    private ArchiveDetailResponse createArchiveDetailResponse(Long id, int status) {
        ArchiveDetailResponse response = new ArchiveDetailResponse();
        response.setId(id);
        response.setPdiTaskId(id + 1000);

        ArchiveDetailResponse.ChannelInfo channel = new ArchiveDetailResponse.ChannelInfo();
        channel.setId(101L);
        channel.setName("左前门通道");
        channel.setAlgorithmType("pdi_left_front");
        response.setChannel(channel);

        ArchiveDetailResponse.SiteInfo site = new ArchiveDetailResponse.SiteInfo();
        site.setId(1L);
        site.setName("金桥库");
        site.setCode("JQ");
        response.setSite(site);

        response.setStartTime(LocalDateTime.of(2026, 3, 25, 10, 0, 0));
        response.setEndTime(LocalDateTime.of(2026, 3, 25, 10, 8, 32));
        response.setEstimatedDuration(10);
        response.setActualDuration(512);
        response.setStatus(status);
        response.setStatusText(ArchiveStatus.fromCode(status).getText());
        response.setTimeline(List.of());

        return response;
    }
}
