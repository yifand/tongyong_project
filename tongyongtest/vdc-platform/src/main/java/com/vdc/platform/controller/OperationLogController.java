package com.vdc.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vdc.platform.common.ApiResult;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.service.IOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/system/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final IOperationLogService operationLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('config:read') or hasAuthority('admin')")
    public ApiResult<Page<OperationLog>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Page<OperationLog> pager = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreatedAt);

        if (operationType != null && !operationType.isBlank()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreatedAt, endTime);
        }

        return ApiResult.success(operationLogService.page(pager, wrapper));
    }
}
