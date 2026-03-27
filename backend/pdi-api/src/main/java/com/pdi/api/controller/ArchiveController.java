package com.pdi.api.controller;

import com.pdi.api.aspect.OperationLog;
import com.pdi.api.dto.ArchiveQueryDTO;
import com.pdi.api.vo.ArchiveDetailVO;
import com.pdi.api.vo.ArchiveVO;
import com.pdi.api.vo.ExportResultVO;
import com.pdi.api.vo.TimelineVO;
import com.pdi.common.result.PageResult;
import com.pdi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 行为档案控制器
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@RestController
@RequestMapping("/api/v1/archives")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "行为档案", description = "PDI作业档案查询、时间线、下载等接口")
public class ArchiveController {

    // TODO: 注入ArchiveService
    // private final ArchiveService archiveService;

    /**
     * 获取档案列表
     */
    @GetMapping
    @Operation(summary = "获取档案列表", description = "获取PDI作业档案列表")
    public Result<PageResult<ArchiveVO>> listArchives(ArchiveQueryDTO query) {
        log.info("获取档案列表");
        // TODO: 调用archiveService.listArchives(query)
        return Result.success();
    }

    /**
     * 获取档案详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取档案详情", description = "获取PDI作业档案详情")
    @Parameter(name = "id", description = "档案ID", required = true)
    public Result<ArchiveDetailVO> getArchiveDetail(@PathVariable Long id) {
        log.info("获取档案详情: {}", id);
        // TODO: 调用archiveService.getArchiveDetail(id)
        return Result.success();
    }

    /**
     * 获取档案时间线
     */
    @GetMapping("/{id}/timeline")
    @Operation(summary = "获取档案时间线", description = "获取PDI作业的时间线数据")
    @Parameter(name = "id", description = "档案ID", required = true)
    public Result<TimelineVO> getArchiveTimeline(@PathVariable Long id) {
        log.info("获取档案时间线: {}", id);
        // TODO: 调用archiveService.getTimeline(id)
        return Result.success();
    }

    /**
     * 下载档案图片包
     */
    @GetMapping("/{id}/download")
    @OperationLog(module = "行为档案", operation = "下载档案")
    @Operation(summary = "下载档案图片包", description = "生成并获取档案图片包下载链接")
    @Parameter(name = "id", description = "档案ID", required = true)
    public Result<ExportResultVO> downloadArchiveImages(@PathVariable Long id) {
        log.info("下载档案图片包: {}", id);
        // TODO: 调用archiveService.generateImagePackage(id)
        return Result.success();
    }
}
