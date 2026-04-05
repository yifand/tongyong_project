package com.vdc.pdi.behaviorarchive.controller;

import com.vdc.pdi.behaviorarchive.dto.request.ArchiveListRequest;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveDetailResponse;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveResponse;
import com.vdc.pdi.behaviorarchive.service.ArchiveExportService;
import com.vdc.pdi.behaviorarchive.service.BehaviorArchiveService;
import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * 行为档案控制器
 */
@RestController
@RequestMapping("/api/v1/archives")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "行为档案管理", description = "PDI作业行为档案相关接口")
public class BehaviorArchiveController {

    private final BehaviorArchiveService archiveService;
    private final ArchiveExportService exportService;

    /**
     * 档案列表查询
     */
    @GetMapping
    @Operation(summary = "档案列表查询", description = "分页查询行为档案列表，支持多种筛选条件")
    public ApiResponse<PageResult<ArchiveResponse>> listArchives(
            @Valid @ModelAttribute ArchiveListRequest request,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.debug("查询档案列表, page={}, size={}, siteId={}", page, size, request.getSiteId());
        PageResult<ArchiveResponse> result = archiveService.queryArchiveList(request, page, size);
        return ApiResponse.success(result);
    }

    /**
     * 档案详情查询
     */
    @GetMapping("/{id}")
    @Operation(summary = "档案详情查询", description = "查询指定档案的详细信息，包含时间线数据")
    public ApiResponse<ArchiveDetailResponse> getArchiveDetail(
            @Parameter(description = "档案ID") @PathVariable Long id) {

        log.debug("查询档案详情, id={}", id);
        ArchiveDetailResponse detail = archiveService.getArchiveDetail(id);
        return ApiResponse.success(detail);
    }

    /**
     * 图片包下载
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "图片包下载", description = "下载指定档案的图片包（ZIP格式）")
    public ResponseEntity<StreamingResponseBody> downloadImagePackage(
            @Parameter(description = "档案ID") @PathVariable Long id) {

        log.debug("下载图片包, archiveId={}", id);
        return exportService.downloadImagePackage(id, null);
    }
}
