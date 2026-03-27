package com.pdi.service.alarm;

import com.pdi.common.result.PageResult;
import com.pdi.service.alarm.dto.AlarmCreateDTO;
import com.pdi.service.alarm.dto.AlarmHandleDTO;
import com.pdi.service.alarm.dto.AlarmQueryDTO;
import com.pdi.service.alarm.dto.AlarmStatisticsDTO;
import com.pdi.service.alarm.vo.AlarmDetailVO;
import com.pdi.service.alarm.vo.AlarmVO;

import java.util.List;

/**
 * 预警服务接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface AlarmService {

    /**
     * 创建预警（供数据接收服务调用）
     *
     * @param dto 预警信息
     * @return 创建的预警信息
     */
    AlarmVO createAlarm(AlarmCreateDTO dto);

    /**
     * 实时预警列表（未处理）
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AlarmVO> listRealTimeAlarms(AlarmQueryDTO query);

    /**
     * 历史预警列表（支持筛选）
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AlarmVO> listHistoryAlarms(AlarmQueryDTO query);

    /**
     * 获取预警详情
     *
     * @param alarmId 预警ID
     * @return 预警详情
     */
    AlarmDetailVO getAlarmDetail(Long alarmId);

    /**
     * 处理预警
     *
     * @param alarmId 预警ID
     * @param dto     处理信息
     */
    void handleAlarm(Long alarmId, AlarmHandleDTO dto);

    /**
     * 标记误报
     *
     * @param alarmId 预警ID
     * @param reason  误报原因
     */
    void markAsFalseAlarm(Long alarmId, String reason);

    /**
     * 获取预警统计
     *
     * @param query 查询条件
     * @return 统计信息
     */
    AlarmStatisticsDTO getStatistics(AlarmQueryDTO query);

    /**
     * 获取最近报警列表
     *
     * @param limit 条数限制
     * @return 报警列表
     */
    List<AlarmVO> getRecentAlarms(Integer limit);

    /**
     * 推送预警到前端
     *
     * @param alarm 预警信息
     */
    void pushAlarm(AlarmVO alarm);

}
