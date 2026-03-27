package com.pdi.api.controller;

import com.pdi.api.aspect.OperationLog;
import com.pdi.api.dto.*;
import com.pdi.api.vo.AlarmDetailVO;
import com.pdi.api.vo.AlarmVO;
import com.pdi.api.vo.ExportResultVO;
import com.pdi.api.vo.ImageDownloadVO;
import com.pdi.common.result.PageResult;
import com.pdi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预警中心控制器
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "预警中心", description = "实时预警、历史预警、预警处理等接口")
public class AlarmController {

    // TODO: 注入AlarmService
    // private final AlarmService alarmService;

    /**
     * 获取实时预警列表
     */
    @GetMapping("/realtime")
    @Operation(summary = "获取实时预警", description = "获取实时未处理预警列表")
    public Result<PageResult<AlarmVO>> listRealTimeAlarms(AlarmQueryDTO query) {
        log.info("获取实时预警列表");
        // TODO: 调用alarmService.listRealTimeAlarms(query)
        return Result.success();
    }

    /**
     * 获取历史预警列表
     */
    @GetMapping("/history")
    @Operation(summary = "获取历史预警", description = "获取历史预警列表(支持分页和筛选)")
    public Result<PageResult<AlarmVO>> listHistoryAlarms(AlarmQueryDTO query) {
        log.info("获取历史预警列表");
        // TODO: 调用alarmService.listHistoryAlarms(query)
        return Result.success();
    }

    /**
     * 获取预警详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取预警详情", description = "获取指定预警的详细信息")
    @Parameter(name = "id", description = "预警ID", required = true)
    public Result<AlarmDetailVO> getAlarmDetail(@PathVariable Long id) {
        log.info("获取预警详情: {}", id);
        // TODO: 调用alarmService.getAlarmDetail(id)
        return Result.success();
    }

    /**
     * 标记预警已处理
     */
    @PutMapping("/{id}/handle")
    @OperationLog(module = "预警中心", operation = "处理预警")
    @Operation(summary = "处理预警", description = "标记预警为已处理状态")
    @Parameter(name = "id", description = "预警ID", required = true)
    public Result<AlarmVO> handleAlarm(
            @PathVariable Long id,
            @Valid @RequestBody AlarmHandleDTO handleDTO) {
        log.info("处理预警: {}", id);
        // TODO: 调用alarmService.handleAlarm(id, handleDTO)
        return Result.success();
    }

    /**
     * 标记预警为误报
     */
    @PutMapping("/{id}/false-positive")
    @OperationLog(module = "预警中心", operation = "标记误报")
    @Operation(summary = "标记误报", description = "标记预警为误报")
    @Parameter(name = "id", description = "预警ID", required = true)
    public Result<AlarmVO> markAsFalsePositive(
            @PathVariable Long id,
            @Valid @RequestBody FalseAlarmDTO falseAlarmDTO) {
        log.info("标记误报: {}", id);
        // TODO: 调用alarmService.markAsFalsePositive(id, falseAlarmDTO)
        return Result.success();
    }

    /**
     * 下载报警图片
     */
    @GetMapping("/{id}/images")
    @Operation(summary = "下载报警图片", description = "获取报警图片下载链接")
    @Parameter(name = "id", description = "预警ID", required = true)
    public Result<List<ImageDownloadVO>> downloadAlarmImages(@PathVariable Long id) {
        log.info("下载报警图片: {}", id);
        // TODO: 调用alarmService.getAlarmImages(id)
        return Result.success();
    }

    /**
     * 导出历史预警
     */
    @PostMapping("/export")
    @OperationLog(module = "预警中心", operation = "导出预警")
    @Operation(summary = "导出预警", description = "导出历史预警数据")
    public Result<ExportResultVO> exportAlarms(@RequestBody AlarmExportDTO exportDTO) {
        log.info("导出历史预警");
        // TODO: 调用alarmService.exportAlarms(exportDTO)
        return Result.success();
    }
}
