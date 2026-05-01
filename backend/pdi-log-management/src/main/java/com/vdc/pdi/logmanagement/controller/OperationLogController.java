package com.vdc.pdi.logmanagement.controller;

import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.dto.request.OperationLogRequest;
import com.vdc.pdi.logmanagement.dto.response.OperationLogResponse;
import com.vdc.pdi.logmanagement.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/v1/logs/operations")
@Tag(name = "操作日志管理", description = "操作日志查询接口")
@RequiredArgsConstructor
@Validated
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    @Operation(summary = "操作日志分页查询", description = "支持按时间、用户、操作类型、结果筛选")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SITE_ADMIN')")
    public ApiResponse<PageResult<OperationLogResponse>> queryOperationLogs(
            @Valid @ModelAttribute OperationLogRequest request) {
        PageResult<OperationLogResponse> result = operationLogService.queryOperationLogs(request);
        return ApiResponse.success(result);
    }

    /**
     * 导出操作日志
     */
    @GetMapping("/export")
    @Operation(summary = "导出操作日志", description = "导出CSV格式的操作日志")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void exportOperationLogs(
            @Valid @ModelAttribute OperationLogRequest request,
            HttpServletResponse response) {
        operationLogService.exportOperationLogs(request, response);
    }

    /**
     * 获取操作类型列表
     */
    @GetMapping("/operation-types")
    @Operation(summary = "获取操作类型列表")
    public ApiResponse<List<Map<String, Object>>> getOperationTypes() {
        return ApiResponse.success(operationLogService.getOperationTypes());
    }
}
