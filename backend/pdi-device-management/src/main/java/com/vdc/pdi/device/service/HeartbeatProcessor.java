package com.vdc.pdi.device.service;

import com.vdc.pdi.device.config.DeviceConfig;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import com.vdc.pdi.device.domain.vo.HeartbeatInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 心跳处理器
 * 处理边缘盒子上报的心跳数据
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HeartbeatProcessor {

    private final EdgeBoxRepository edgeBoxRepository;
    private final DeviceConfig deviceConfig;

    /**
     * 处理心跳事件
     * 由AlgorithmInlet模块发布心跳事件后调用
     */
    @EventListener
    @Transactional
    public void handleHeartbeat(HeartbeatEvent event) {
        log.debug("收到心跳: boxId={}, timestamp={}",
                event.getBoxId(), event.getTimestamp());

        // 查询盒子
        EdgeBox box = edgeBoxRepository.findById(event.getBoxId())
                .orElse(null);

        if (box == null || box.getDeletedAt() != null) {
            log.warn("心跳对应的盒子不存在或已删除: boxId={}", event.getBoxId());
            return;
        }

        // 更新盒子状态
        box.setStatus(1);  // 在线
        box.setLastHeartbeatAt(event.getTimestamp());

        // 更新资源使用率
        if (event.getCpuUsage() != null) {
            box.setCpuUsage(event.getCpuUsage());
        }
        if (event.getMemoryUsage() != null) {
            box.setMemoryUsage(event.getMemoryUsage());
        }
        if (event.getDiskUsage() != null) {
            box.setDiskUsage(event.getDiskUsage());
        }
        if (event.getVersion() != null) {
            box.setVersion(event.getVersion());
        }

        edgeBoxRepository.save(box);
        log.debug("心跳处理完成: boxId={}", event.getBoxId());
    }

    /**
     * 定时检测心跳超时
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkHeartbeatTimeout() {
        if (!deviceConfig.getEnableAutoOffline()) {
            return;
        }

        int timeoutSeconds = deviceConfig.getHeartbeatTimeout();
        LocalDateTime timeoutTime = LocalDateTime.now().minusSeconds(timeoutSeconds);

        List<EdgeBox> timeoutBoxes = edgeBoxRepository.findTimeoutBoxes(timeoutTime);

        for (EdgeBox box : timeoutBoxes) {
            box.setStatus(0);  // 离线
            edgeBoxRepository.save(box);
            log.info("心跳超时，盒子设为离线: boxId={}, name={}, lastHeartbeat={}",
                    box.getId(), box.getName(), box.getLastHeartbeatAt());
        }

        if (!timeoutBoxes.isEmpty()) {
            log.info("心跳超时检测完成，共标记{}个盒子为离线", timeoutBoxes.size());
        }
    }

    /**
     * 心跳事件
     * 用于Spring事件机制
     */
    public static class HeartbeatEvent {
        private final Long boxId;
        private final LocalDateTime timestamp;
        private final Double cpuUsage;
        private final Double memoryUsage;
        private final Double diskUsage;
        private final String version;

        public HeartbeatEvent(HeartbeatInfo info) {
            this.boxId = info.getBoxId();
            this.timestamp = info.getTimestamp();
            this.cpuUsage = info.getCpuUsage();
            this.memoryUsage = info.getMemoryUsage();
            this.diskUsage = info.getDiskUsage();
            this.version = info.getVersion();
        }

        // Getters
        public Long getBoxId() { return boxId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Double getCpuUsage() { return cpuUsage; }
        public Double getMemoryUsage() { return memoryUsage; }
        public Double getDiskUsage() { return diskUsage; }
        public String getVersion() { return version; }
    }
}
