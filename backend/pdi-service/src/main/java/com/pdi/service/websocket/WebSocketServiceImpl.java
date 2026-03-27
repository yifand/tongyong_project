package com.pdi.service.websocket;

import com.pdi.service.alarm.vo.AlarmVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendAlarm(AlarmVO alarm) {
        try {
            messagingTemplate.convertAndSend("/topic/alarms", alarm);
            log.debug("推送报警到全局: alarmId={}", alarm.getId());
        } catch (Exception e) {
            log.error("推送报警失败: alarmId={}", alarm.getId(), e);
        }
    }

    @Override
    public void sendAlarmToSite(AlarmVO alarm, Long siteId) {
        try {
            messagingTemplate.convertAndSend("/topic/alarms/site/" + siteId, alarm);
            log.debug("推送报警到站点: alarmId={}, siteId={}", alarm.getId(), siteId);
        } catch (Exception e) {
            log.error("推送站点报警失败: alarmId={}, siteId={}", alarm.getId(), siteId, e);
        }
    }

    @Override
    public void sendDeviceStatus(Long boxId, Integer status) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("boxId", boxId);
            payload.put("status", status);
            payload.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/device/status", payload);
            log.debug("推送设备状态: boxId={}, status={}", boxId, status);
        } catch (Exception e) {
            log.error("推送设备状态失败: boxId={}", boxId, e);
        }
    }

    @Override
    public void broadcastStatistics() {
        try {
            Map<String, Object> stats = buildStatistics();
            messagingTemplate.convertAndSend("/topic/statistics", stats);
            log.debug("广播统计更新");
        } catch (Exception e) {
            log.error("广播统计更新失败", e);
        }
    }

    @Override
    public void sendStatisticsToSite(Long siteId) {
        try {
            Map<String, Object> stats = buildStatistics(siteId);
            messagingTemplate.convertAndSend("/topic/statistics/site/" + siteId, stats);
            log.debug("发送统计更新到站点: siteId={}", siteId);
        } catch (Exception e) {
            log.error("发送站点统计更新失败: siteId={}", siteId, e);
        }
    }

    @Override
    public void sendToUser(String userId, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(userId, destination, payload);
            log.debug("发送消息给用户: userId={}, destination={}", userId, destination);
        } catch (Exception e) {
            log.error("发送消息给用户失败: userId={}", userId, e);
        }
    }

    /**
     * 构建全局统计数据
     */
    private Map<String, Object> buildStatistics() {
        Map<String, Object> stats = new HashMap<>();
        // TODO: 从数据库或缓存获取实时统计数据
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("todayAlarms", 0);
        stats.put("unhandledAlarms", 0);
        stats.put("onlineBoxes", 0);
        stats.put("totalBoxes", 0);
        stats.put("onlineRate", 0);
        return stats;
    }

    /**
     * 构建站点统计数据
     */
    private Map<String, Object> buildStatistics(Long siteId) {
        Map<String, Object> stats = new HashMap<>();
        // TODO: 从数据库或缓存获取站点实时统计数据
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("siteId", siteId);
        stats.put("todayAlarms", 0);
        stats.put("unhandledAlarms", 0);
        return stats;
    }

}
