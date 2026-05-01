package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogCleanupService {

    private final OperationLogMapper operationLogMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldOperationLogs() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(180);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getCreatedAt, threshold);
        int deleted = operationLogMapper.delete(wrapper);
        log.info("Cleaned up {} operation logs older than {} days", deleted, 180);
    }
}
