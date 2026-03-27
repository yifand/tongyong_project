package com.pdi.service.log;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pdi.common.result.PageResult;
import com.pdi.dao.entity.OperationLog;

import java.time.LocalDateTime;

/**
 * 操作日志服务接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 异步保存操作日志
     *
     * @param log 日志信息
     */
    void saveAsync(OperationLog log);

    /**
     * 分页查询操作日志
     *
     * @param module    操作模块
     * @param username  用户名
     * @param status    状态: 0-失败, 1-成功
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param page      页码
     * @param size      每页大小
     * @return 分页结果
     */
    PageResult<OperationLog> listOperationLogs(String module, String username, Integer status,
                                                LocalDateTime startTime, LocalDateTime endTime,
                                                Long page, Long size);

    /**
     * 清理指定时间之前的日志
     *
     * @param before 截止时间
     */
    void cleanLogs(LocalDateTime before);

}
