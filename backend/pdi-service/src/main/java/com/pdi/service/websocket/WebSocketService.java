package com.pdi.service.websocket;

import com.pdi.service.alarm.vo.AlarmVO;

/**
 * WebSocket服务接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface WebSocketService {

    /**
     * 推送报警消息
     *
     * @param alarm 报警信息
     */
    void sendAlarm(AlarmVO alarm);

    /**
     * 推送报警到指定站点
     *
     * @param alarm  报警信息
     * @param siteId 站点ID
     */
    void sendAlarmToSite(AlarmVO alarm, Long siteId);

    /**
     * 推送设备状态变更
     *
     * @param boxId  盒子ID
     * @param status 状态: 0-离线, 1-在线, 2-故障
     */
    void sendDeviceStatus(Long boxId, Integer status);

    /**
     * 广播统计更新
     */
    void broadcastStatistics();

    /**
     * 发送统计更新到指定站点
     *
     * @param siteId 站点ID
     */
    void sendStatisticsToSite(Long siteId);

    /**
     * 发送消息给指定用户
     *
     * @param userId      用户ID
     * @param destination 目标地址
     * @param payload     消息内容
     */
    void sendToUser(String userId, String destination, Object payload);

}
