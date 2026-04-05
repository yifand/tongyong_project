package com.vdc.pdi.device.task;

import com.vdc.pdi.device.service.BoxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设备离线检测定时任务
 * 定期检查设备心跳，将超时的设备标记为离线
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceOfflineCheckTask {

    private final BoxService boxService;

    /**
     * 每分钟检查一次离线状态
     */
    @Scheduled(fixedRate = 60000)
    public void checkOfflineStatus() {
        log.debug("开始执行设备离线状态检查");
        try {
            boxService.checkAndUpdateOfflineStatus();
            log.debug("设备离线状态检查完成");
        } catch (Exception e) {
            log.error("设备离线状态检查失败", e);
        }
    }
}
