package com.pdi.receiver.service;

import com.pdi.common.enums.AlarmHandleStatusEnum;
import com.pdi.common.enums.AlarmLevelEnum;
import com.pdi.common.enums.AlarmTypeEnum;
import com.pdi.dao.entity.Alarm;
import com.pdi.dao.entity.Channel;
import com.pdi.dao.entity.Site;
import com.pdi.dao.mapper.AlarmMapper;
import com.pdi.dao.mapper.ChannelMapper;
import com.pdi.dao.mapper.SiteMapper;
import com.pdi.receiver.dto.AlarmEventDTO;
import com.pdi.receiver.statemachine.PDITaskManager;
import com.pdi.receiver.websocket.AlarmVO;
import com.pdi.receiver.websocket.AlarmWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 报警事件处理服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class AlarmEventServiceImpl implements AlarmEventService {

    private static final String ALARM_CACHE_KEY = "alarm:dup:";
    private static final long ALARM_DUPLICATE_DURATION = 5; // 5分钟内不重复报警

    @Autowired
    private AlarmMapper alarmMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private PDITaskManager taskManager;

    @Autowired
    private AlarmWebSocketHandler webSocketHandler;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processAlarmEvent(AlarmEventDTO dto) {
        // 1. 检查重复报警
        if (isDuplicateAlarm(dto.getChannelId(), dto.getAlarmType())) {
            log.warn("重复报警，忽略: channelId={}, alarmType={}", dto.getChannelId(), dto.getAlarmType());
            return;
        }

        // 2. 获取通道和站点信息
        Channel channel = channelMapper.selectById(dto.getChannelId());
        if (channel == null) {
            log.error("通道不存在: {}", dto.getChannelId());
            return;
        }

        Site site = siteMapper.selectById(dto.getSiteId() != null ? dto.getSiteId() : channel.getSiteId());

        // 3. 获取当前PDI任务（如果有）
        Long currentTaskId = taskManager.getCurrentTask(dto.getChannelId());
        if (dto.getPdiTaskId() != null) {
            currentTaskId = dto.getPdiTaskId();
        }

        // 4. 保存报警记录
        Alarm alarm = convertToEntity(dto, channel, site, currentTaskId);
        alarmMapper.insert(alarm);

        // 5. 设置重复报警缓存
        setDuplicateAlarmCache(dto.getChannelId(), dto.getAlarmType());

        // 6. 转换为VO并推送
        AlarmVO alarmVO = convertToVO(alarm, channel, site);
        webSocketHandler.sendAlarm(alarmVO);

        log.info("报警已处理并推送: alarmId={}, alarmType={}, channelId={}", 
                alarm.getId(), dto.getAlarmType(), dto.getChannelId());
    }

    @Override
    public boolean isDuplicateAlarm(Long channelId, Integer alarmType) {
        String key = ALARM_CACHE_KEY + channelId + ":" + alarmType;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置重复报警缓存
     */
    private void setDuplicateAlarmCache(Long channelId, Integer alarmType) {
        String key = ALARM_CACHE_KEY + channelId + ":" + alarmType;
        redisTemplate.opsForValue().set(key, 1, ALARM_DUPLICATE_DURATION, TimeUnit.MINUTES);
    }

    /**
     * 转换为实体
     */
    private Alarm convertToEntity(AlarmEventDTO dto, Channel channel, Site site, Long taskId) {
        Alarm alarm = new Alarm();
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setAlarmType(dto.getAlarmType());
        alarm.setAlarmLevel(dto.getAlarmLevel() != null ? dto.getAlarmLevel() : AlarmLevelEnum.MEDIUM.getCode());
        alarm.setSiteId(site != null ? site.getId() : dto.getSiteId());
        alarm.setBoxId(dto.getBoxId() != null ? dto.getBoxId() : channel.getBoxId());
        alarm.setChannelId(dto.getChannelId());
        alarm.setSiteName(site != null ? site.getSiteName() : null);
        alarm.setChannelName(channel != null ? channel.getChannelName() : null);
        alarm.setPdiTaskId(taskId);
        
        // 设置报警标题和描述
        String title = dto.getAlarmTitle();
        String desc = dto.getAlarmDesc();
        if (title == null && dto.getAlarmType() != null) {
            AlarmTypeEnum typeEnum = AlarmTypeEnum.getByCode(dto.getAlarmType());
            if (typeEnum != null) {
                title = typeEnum.getDefaultTitle();
                if (desc == null) {
                    desc = typeEnum.getName();
                }
            }
        }
        alarm.setAlarmTitle(title != null ? title : "未知报警");
        alarm.setAlarmDesc(desc);
        
        alarm.setImageUrl(dto.getImageUrl());
        alarm.setVideoUrl(dto.getVideoUrl());
        alarm.setHandleStatus(AlarmHandleStatusEnum.UNHANDLED.getCode());
        
        return alarm;
    }

    /**
     * 转换为VO
     */
    private AlarmVO convertToVO(Alarm alarm, Channel channel, Site site) {
        AlarmVO vo = new AlarmVO();
        BeanUtils.copyProperties(alarm, vo);
        
        // 设置枚举名称
        AlarmTypeEnum typeEnum = AlarmTypeEnum.getByCode(alarm.getAlarmType());
        if (typeEnum != null) {
            vo.setAlarmTypeName(typeEnum.getName());
        }
        
        AlarmLevelEnum levelEnum = AlarmLevelEnum.getByCode(alarm.getAlarmLevel());
        if (levelEnum != null) {
            vo.setAlarmLevelName(levelEnum.getName());
        }
        
        AlarmHandleStatusEnum statusEnum = AlarmHandleStatusEnum.getByCode(alarm.getHandleStatus());
        if (statusEnum != null) {
            vo.setHandleStatusName(statusEnum.getName());
        }
        
        return vo;
    }

}
