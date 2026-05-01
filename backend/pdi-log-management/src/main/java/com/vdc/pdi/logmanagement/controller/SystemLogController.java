package com.vdc.pdi.logmanagement.controller;

import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.dto.request.SystemLogRequest;
import com.vdc.pdi.logmanagement.dto.response.SystemLogResponse;
import com.vdc.pdi.logmanagement.service.SystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统日志控制器
 */
@RestController
@RequestMapping("/api/v1/logs/system")
@Tag(name = "系统日志管理", description = "系统日志查询接口")
@RequiredArgsConstructor
@Validated
public class SystemLogController {

    private final SystemLogService systemLogService;

    /**
     * 分页查询系统日志
     */
    @GetMapping
    @Operation(summary = "系统日志分页查询", description = "支持按日志级别、时间范围筛选")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<PageResult<SystemLogResponse>> querySystemLogs(
            @Valid @ModelAttribute SystemLogRequest request) {
        PageResult<SystemLogResponse> result = systemLogService.querySystemLogs(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取日志级别列表
     */
    @GetMapping("/levels")
    @Operation(summary = "获取日志级别列表")
    public ApiResponse<List<Map<String, Object>>> getLogLevels() {
        return ApiResponse.success(systemLogService.getLogLevels());
    }
}
