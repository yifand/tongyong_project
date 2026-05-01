package com.vdc.pdi.logmanagement.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.domain.entity.QSystemLog;
import com.vdc.pdi.logmanagement.domain.entity.SystemLog;
import com.vdc.pdi.logmanagement.domain.repository.SystemLogRepository;
import com.vdc.pdi.logmanagement.dto.request.SystemLogRequest;
import com.vdc.pdi.logmanagement.dto.response.SystemLogResponse;
import com.vdc.pdi.logmanagement.enums.LogLevel;
import com.vdc.pdi.logmanagement.mapper.LogMapper;
import com.vdc.pdi.logmanagement.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统日志服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private final LogMapper logMapper;

    @Override
    public PageResult<SystemLogResponse> querySystemLogs(SystemLogRequest request) {
        QSystemLog qSystemLog = QSystemLog.systemLog;
        BooleanBuilder builder = new BooleanBuilder();

        // 日志级别筛选
        if (request.getLevel() != null) {
            builder.and(qSystemLog.level.eq(request.getLevel()));
        }

        // 模块筛选
        if (StringUtils.hasText(request.getModule())) {
            builder.and(qSystemLog.module.containsIgnoreCase(request.getModule()));
        }

        // 时间范围筛选
        if (request.getStartTime() != null) {
            builder.and(qSystemLog.createdAt.goe(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            builder.and(qSystemLog.createdAt.loe(request.getEndTime()));
        }

        // 关键字搜索
        if (StringUtils.hasText(request.getKeyword())) {
            builder.and(qSystemLog.message.containsIgnoreCase(request.getKeyword()));
        }

        // 执行分页查询
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<SystemLog> page = systemLogRepository.findAll(builder, pageable);

        // 转换并返回
        List<SystemLogResponse> list = logMapper.toSystemLogResponseList(page.getContent());

        return PageResult.of(list, page.getTotalElements(), page.getNumber() + 1, page.getSize());
    }

    @Override
    public void saveSystemLog(SystemLog systemLog) {
        try {
            systemLogRepository.save(systemLog);
        } catch (Exception e) {
            log.error("保存系统日志失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getLogLevels() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (LogLevel level : LogLevel.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", level.getCode());
            map.put("name", level.getName());
            map.put("description", level.getDescription());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public int cleanupExpiredLogs(LocalDateTime before) {
        return systemLogRepository.deleteByCreatedAtBefore(before);
    }
}
