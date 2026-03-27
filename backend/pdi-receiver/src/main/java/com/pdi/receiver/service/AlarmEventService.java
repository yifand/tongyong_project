package com.pdi.receiver.service;

import com.pdi.receiver.dto.AlarmEventDTO;

/**
 * 报警事件处理服务
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface AlarmEventService {

    /**
     * 处理报警事件
     *
     * @param dto 报警事件DTO
     */
    void processAlarmEvent(AlarmEventDTO dto);

    /**
     * 检查是否为重复报警
     *
     * @param channelId 通道ID
     * @param alarmType 报警类型
     * @return true-是重复报警
     */
    boolean isDuplicateAlarm(Long channelId, Integer alarmType);

}
