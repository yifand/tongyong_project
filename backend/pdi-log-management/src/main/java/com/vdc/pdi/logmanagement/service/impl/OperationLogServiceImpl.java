package com.vdc.pdi.logmanagement.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.common.utils.SecurityUtils;
import com.vdc.pdi.logmanagement.domain.entity.OperationLog;
import com.vdc.pdi.logmanagement.domain.entity.QOperationLog;
import com.vdc.pdi.logmanagement.domain.repository.OperationLogRepository;
import com.vdc.pdi.logmanagement.dto.request.OperationLogRequest;
import com.vdc.pdi.logmanagement.dto.response.OperationLogResponse;
import com.vdc.pdi.logmanagement.enums.OperationType;
import com.vdc.pdi.logmanagement.mapper.LogMapper;
import com.vdc.pdi.logmanagement.service.OperationLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 操作日志服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final LogMapper logMapper;

    @Override
    public PageResult<OperationLogResponse> queryOperationLogs(OperationLogRequest request) {
        QOperationLog qOperationLog = QOperationLog.operationLog;
        BooleanBuilder builder = new BooleanBuilder();

        // 站点隔离 (简化实现，实际应根据当前用户站点ID过滤)
        // TODO: 根据实际业务需求实现站点隔离

        // 时间范围筛选
        if (request.getStartTime() != null) {
            builder.and(qOperationLog.createdAt.goe(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            builder.and(qOperationLog.createdAt.loe(request.getEndTime()));
        }

        // 用户筛选
        if (StringUtils.hasText(request.getUsername())) {
            builder.and(qOperationLog.username.containsIgnoreCase(request.getUsername()));
        }

        // 操作类型筛选
        if (request.getOperationType() != null) {
            builder.and(qOperationLog.operationType.eq(request.getOperationType()));
        }

        // 结果筛选
        if (request.getResult() != null) {
            builder.and(qOperationLog.result.eq(request.getResult()));
        }

        // 执行分页查询
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<OperationLog> page = operationLogRepository.findAll(builder, pageable);

        // 转换并返回
        List<OperationLogResponse> list = logMapper.toOperationLogResponseList(page.getContent());

        return PageResult.of(list, page.getTotalElements(), page.getNumber() + 1, page.getSize());
    }

    @Override
    @Async("logExecutor")
    public void saveOperationLogAsync(OperationLog operationLog) {
        try {
            operationLogRepository.save(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    public void exportOperationLogs(OperationLogRequest request, HttpServletResponse response) {
        // 设置响应头
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=operation_logs.csv");

        try (PrintWriter writer = response.getWriter()) {
            // 写入BOM，解决中文乱码
            writer.write('\ufeff');
            // 写入表头
            writer.println("ID,用户名,IP地址,操作类型,操作详情,请求参数,结果,错误信息,执行时长(ms),操作时间");

            // 查询数据并写入（简化实现，实际应分页处理大量数据）
            PageResult<OperationLogResponse> result = queryOperationLogs(request);
            for (OperationLogResponse item : result.getList()) {
                writer.printf("%d,%s,%s,%s,\"%s\",\"%s\",%s,\"%s\",%d,%s%n",
                    item.getId(),
                    escapeCsv(item.getUsername()),
                    item.getIpAddress(),
                    item.getOperationType(),
                    escapeCsv(item.getOperationDetail()),
                    escapeCsv(item.getRequestParams()),
                    item.getResult() == 1 ? "成功" : "失败",
                    escapeCsv(item.getErrorMsg()),
                    item.getExecutionTime(),
                    item.getCreatedAt()
                );
            }
        } catch (IOException e) {
            log.error("导出操作日志失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getOperationTypes() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (OperationType type : OperationType.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("name", type.getName());
            map.put("description", type.getDescription());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public int cleanupExpiredLogs(LocalDateTime before) {
        return operationLogRepository.deleteByCreatedAtBefore(before);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 替换换行符和引号
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ").replace(",", "，");
    }
}
