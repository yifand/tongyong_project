package com.vdc.pdi.logmanagement.service;

import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.logmanagement.domain.entity.SystemLog;
import com.vdc.pdi.logmanagement.dto.request.SystemLogRequest;
import com.vdc.pdi.logmanagement.dto.response.SystemLogResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统日志服务接口
 */
public interface SystemLogService {

    /**
     * 分页查询系统日志
     */
    PageResult<SystemLogResponse> querySystemLogs(SystemLogRequest request);

    /**
     * 保存系统日志
     */
    void saveSystemLog(SystemLog log);

    /**
     * 获取日志级别列表
     */
    List<Map<String, Object>> getLogLevels();

    /**
     * 清理过期系统日志
     */
    int cleanupExpiredLogs(LocalDateTime before);
}
