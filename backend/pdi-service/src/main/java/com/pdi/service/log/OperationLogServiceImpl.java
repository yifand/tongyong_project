package com.pdi.service.log;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdi.common.result.PageResult;
import com.pdi.dao.entity.OperationLog;
import com.pdi.dao.mapper.OperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    @Override
    @Async("logExecutor")
    public void saveAsync(OperationLog operationLog) {
        save(operationLog);
    }

    @Override
    public boolean save(OperationLog operationLog) {
        if (operationLog.getLogTime() == null) {
            operationLog.setLogTime(LocalDateTime.now());
        }
        return super.save(operationLog);
    }

    @Override
    public PageResult<OperationLog> listOperationLogs(String module, String username, Integer status,
                                                       LocalDateTime startTime, LocalDateTime endTime,
                                                       Long page, Long size) {
        Page<OperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(OperationLog::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getLogTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getLogTime, endTime);
        }

        wrapper.orderByDesc(OperationLog::getLogTime);

        Page<OperationLog> pageResult = page(pageParam, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
    public void cleanLogs(LocalDateTime before) {
        // 默认清理3个月前的日志
        if (before == null) {
            before = LocalDateTime.now().minusMonths(3);
        }

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getLogTime, before);

        int deleted = baseMapper.delete(wrapper);
        log.info("清理操作日志: 删除了{}条记录，截止时间: {}", deleted, before);
    }

}
