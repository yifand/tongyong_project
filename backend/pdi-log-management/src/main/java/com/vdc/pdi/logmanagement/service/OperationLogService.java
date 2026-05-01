package com.vdc.pdi.logmanagement.service;

import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.domain.entity.OperationLog;
import com.vdc.pdi.logmanagement.dto.request.OperationLogRequest;
import com.vdc.pdi.logmanagement.dto.response.OperationLogResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLogResponse> queryOperationLogs(OperationLogRequest request);

    /**
     * 异步保存操作日志
     */
    void saveOperationLogAsync(OperationLog log);

    /**
     * 导出操作日志
     */
    void exportOperationLogs(OperationLogRequest request, HttpServletResponse response);

    /**
     * 获取操作类型列表
     */
    List<Map<String, Object>> getOperationTypes();

    /**
     * 清理过期操作日志
     */
    int cleanupExpiredLogs(LocalDateTime before);
}
