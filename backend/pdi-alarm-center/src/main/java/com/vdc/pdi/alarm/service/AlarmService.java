package com.vdc.pdi.alarm.service;

import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import com.vdc.pdi.alarm.dto.request.AlarmHistoryRequest;
import com.vdc.pdi.alarm.dto.request.AlarmProcessRequest;
import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.dto.response.AlarmStatisticsResponse;
import com.vdc.pdi.common.dto.PageResult;

import java.util.List;

/**
 * 报警服务接口
 */
public interface AlarmService {

    /**
     * 获取实时预警列表
     */
    List<AlarmResponse> getRealtimeAlarms(Integer limit, Integer type);

    /**
     * 历史预警分页查询
     */
    PageResult<AlarmResponse> getHistoryAlarms(AlarmHistoryRequest request);

    /**
     * 获取预警详情
     */
    AlarmResponse getAlarmDetail(Long id);

    /**
     * 标记已处理
     */
    void processAlarm(Long id, AlarmProcessRequest request);

    /**
     * 标记误报
     */
    void markFalsePositive(Long id, AlarmProcessRequest request);

    /**
     * 获取今日统计
     */
    AlarmStatisticsResponse getTodayStatistics();

    /**
     * 创建新报警（由规则引擎调用）
     */
    AlarmRecord createAlarm(AlarmCreateEvent event);
}
