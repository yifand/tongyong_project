package com.pdi.service.alarm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdi.common.enums.AlarmHandleStatusEnum;
import com.pdi.common.enums.AlarmLevelEnum;
import com.pdi.common.enums.AlarmTypeEnum;
import com.pdi.common.exception.BusinessException;
import com.pdi.common.result.PageResult;
import com.pdi.common.result.ResultCode;
import com.pdi.dao.entity.Alarm;
import com.pdi.dao.entity.Box;
import com.pdi.dao.entity.Channel;
import com.pdi.dao.entity.Site;
import com.pdi.dao.mapper.AlarmMapper;
import com.pdi.dao.mapper.BoxMapper;
import com.pdi.dao.mapper.ChannelMapper;
import com.pdi.dao.mapper.SiteMapper;
import com.pdi.service.alarm.dto.AlarmCreateDTO;
import com.pdi.service.alarm.dto.AlarmHandleDTO;
import com.pdi.service.alarm.dto.AlarmQueryDTO;
import com.pdi.service.alarm.dto.AlarmStatisticsDTO;
import com.pdi.service.alarm.vo.AlarmDetailVO;
import com.pdi.service.alarm.vo.AlarmVO;
import com.pdi.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 预警服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class AlarmServiceImpl extends ServiceImpl<AlarmMapper, Alarm> implements AlarmService {

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private BoxMapper boxMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ALARM_NO_PREFIX = "ALM";
    private static final String ALARM_CACHE_KEY = "alarm:realtime:";
    private static final long DUPLICATE_ALARM_INTERVAL = 5; // 5分钟内不重复生成

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlarmVO createAlarm(AlarmCreateDTO dto) {
        // 重复预警检查（5分钟内同一通道同类型不重复生成）
        String dupKey = ALARM_CACHE_KEY + dto.getChannelId() + ":" + dto.getAlarmType();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(dupKey))) {
            log.debug("重复预警过滤: channelId={}, alarmType={}", dto.getChannelId(), dto.getAlarmType());
            return null;
        }

        Alarm alarm = new Alarm();
        BeanUtils.copyProperties(dto, alarm);

        // 设置报警时间
        if (alarm.getAlarmTime() == null) {
            alarm.setAlarmTime(LocalDateTime.now());
        }

        // 设置处理状态为未处理
        alarm.setHandleStatus(AlarmHandleStatusEnum.UNHANDLED.getCode());
        alarm.setCreatedAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());

        save(alarm);

        // 设置重复预警缓存
        redisTemplate.opsForValue().set(dupKey, alarm.getId(), DUPLICATE_ALARM_INTERVAL, TimeUnit.MINUTES);

        AlarmVO alarmVO = convertToVO(alarm);

        // 异步推送
        pushAlarm(alarmVO);

        log.info("创建预警: alarmId={}", alarm.getId());

        return alarmVO;
    }

    @Override
    public PageResult<AlarmVO> listRealTimeAlarms(AlarmQueryDTO query) {
        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();

        // 只查询未处理的预警
        wrapper.eq(Alarm::getHandleStatus, AlarmHandleStatusEnum.UNHANDLED.getCode());

        // 数据权限过滤
        if (query.getSiteId() != null) {
            wrapper.eq(Alarm::getSiteId, query.getSiteId());
        }

        // 报警类型筛选
        if (query.getAlarmType() != null) {
            wrapper.eq(Alarm::getAlarmType, query.getAlarmType());
        }

        // 报警级别筛选
        if (query.getAlarmLevel() != null) {
            wrapper.eq(Alarm::getAlarmLevel, query.getAlarmLevel());
        }

        // 时间范围
        if (query.getStartTime() != null) {
            wrapper.ge(Alarm::getAlarmTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(Alarm::getAlarmTime, query.getEndTime());
        }

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Alarm::getAlarmTitle, query.getKeyword())
                    .or()
                    .like(Alarm::getAlarmDesc, query.getKeyword()));
        }

        wrapper.orderByDesc(Alarm::getAlarmTime);

        Page<Alarm> pageParam = new Page<>(query.getPage(), query.getSize());
        Page<Alarm> pageResult = page(pageParam, wrapper);

        List<AlarmVO> list = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    public PageResult<AlarmVO> listHistoryAlarms(AlarmQueryDTO query) {
        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();

        // 只查询已处理的预警
        wrapper.ne(Alarm::getHandleStatus, AlarmHandleStatusEnum.UNHANDLED.getCode());

        // 数据权限过滤
        if (query.getSiteId() != null) {
            wrapper.eq(Alarm::getSiteId, query.getSiteId());
        }

        // 通道筛选
        if (query.getChannelId() != null) {
            wrapper.eq(Alarm::getChannelId, query.getChannelId());
        }

        // 报警类型筛选
        if (query.getAlarmType() != null) {
            wrapper.eq(Alarm::getAlarmType, query.getAlarmType());
        }

        // 报警级别筛选
        if (query.getAlarmLevel() != null) {
            wrapper.eq(Alarm::getAlarmLevel, query.getAlarmLevel());
        }

        // 处理状态筛选
        if (query.getHandleStatus() != null) {
            wrapper.eq(Alarm::getHandleStatus, query.getHandleStatus());
        }

        // 时间范围
        if (query.getStartTime() != null) {
            wrapper.ge(Alarm::getAlarmTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(Alarm::getAlarmTime, query.getEndTime());
        }

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Alarm::getAlarmTitle, query.getKeyword())
                    .or()
                    .like(Alarm::getAlarmDesc, query.getKeyword()));
        }

        wrapper.orderByDesc(Alarm::getAlarmTime);

        Page<Alarm> pageParam = new Page<>(query.getPage(), query.getSize());
        Page<Alarm> pageResult = page(pageParam, wrapper);

        List<AlarmVO> list = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    public AlarmDetailVO getAlarmDetail(Long alarmId) {
        Alarm alarm = getById(alarmId);
        if (alarm == null) {
            throw new BusinessException(2001, "预警不存在");
        }

        return convertToDetailVO(alarm);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlarm(Long alarmId, AlarmHandleDTO dto) {
        Alarm alarm = getById(alarmId);
        if (alarm == null) {
            throw new BusinessException(2001, "预警不存在");
        }

        if (alarm.getHandleStatus() != AlarmHandleStatusEnum.UNHANDLED.getCode()) {
            throw new BusinessException(2002, "预警已处理，不能重复操作");
        }

        alarm.setHandleStatus(AlarmHandleStatusEnum.HANDLED.getCode());
        alarm.setHandleRemark(dto.getHandleRemark());
        alarm.setHandleTime(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());

        // TODO: 设置处理人ID
        // alarm.setHandleUserId(SecurityUtils.getCurrentUserId());

        updateById(alarm);

        log.info("处理预警: alarmId={}", alarmId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsFalseAlarm(Long alarmId, String reason) {
        Alarm alarm = getById(alarmId);
        if (alarm == null) {
            throw new BusinessException(2001, "预警不存在");
        }

        if (alarm.getHandleStatus() != AlarmHandleStatusEnum.UNHANDLED.getCode()) {
            throw new BusinessException(2002, "预警已处理，不能重复操作");
        }

        alarm.setHandleStatus(AlarmHandleStatusEnum.FALSE_POSITIVE.getCode());
        alarm.setHandleRemark(reason);
        alarm.setHandleTime(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());

        // TODO: 设置处理人ID
        // alarm.setHandleUserId(SecurityUtils.getCurrentUserId());

        updateById(alarm);

        log.info("标记误报: alarmId={}, reason={}", alarmId, reason);
    }

    @Override
    public AlarmStatisticsDTO getStatistics(AlarmQueryDTO query) {
        AlarmStatisticsDTO statistics = new AlarmStatisticsDTO();

        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();

        // 时间范围
        if (query.getStartTime() != null) {
            wrapper.ge(Alarm::getAlarmTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(Alarm::getAlarmTime, query.getEndTime());
        }

        // 站点筛选
        if (query.getSiteId() != null) {
            wrapper.eq(Alarm::getSiteId, query.getSiteId());
        }

        // 总报警数
        long totalAlarms = count(wrapper);
        statistics.setTotalAlarms(totalAlarms);

        // 未处理报警数
        LambdaQueryWrapper<Alarm> unhandledWrapper = wrapper.clone();
        unhandledWrapper.eq(Alarm::getHandleStatus, AlarmHandleStatusEnum.UNHANDLED.getCode());
        statistics.setUnhandledAlarms(count(unhandledWrapper));

        // 已处理报警数
        LambdaQueryWrapper<Alarm> handledWrapper = wrapper.clone();
        handledWrapper.eq(Alarm::getHandleStatus, AlarmHandleStatusEnum.HANDLED.getCode());
        statistics.setHandledAlarms(count(handledWrapper));

        // 误报数
        LambdaQueryWrapper<Alarm> falseAlarmWrapper = wrapper.clone();
        falseAlarmWrapper.eq(Alarm::getHandleStatus, AlarmHandleStatusEnum.FALSE_POSITIVE.getCode());
        statistics.setFalseAlarms(count(falseAlarmWrapper));

        // 按类型统计
        statistics.setAlarmTypeStats(
                lambdaQuery()
                        .apply(query.getStartTime() != null, "alarm_time >= {0}", query.getStartTime())
                        .apply(query.getEndTime() != null, "alarm_time <= {0}", query.getEndTime())
                        .apply(query.getSiteId() != null, "site_id = {0}", query.getSiteId())
                        .list()
                        .stream()
                        .collect(Collectors.groupingBy(Alarm::getAlarmType, Collectors.counting()))
        );

        // 按级别统计
        statistics.setAlarmLevelStats(
                lambdaQuery()
                        .apply(query.getStartTime() != null, "alarm_time >= {0}", query.getStartTime())
                        .apply(query.getEndTime() != null, "alarm_time <= {0}", query.getEndTime())
                        .apply(query.getSiteId() != null, "site_id = {0}", query.getSiteId())
                        .list()
                        .stream()
                        .collect(Collectors.groupingBy(Alarm::getAlarmLevel, Collectors.counting()))
        );

        return statistics;
    }

    @Override
    public List<AlarmVO> getRecentAlarms(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }

        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Alarm::getAlarmTime)
                .last("LIMIT " + limit);

        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Async
    public void pushAlarm(AlarmVO alarm) {
        try {
            webSocketService.sendAlarm(alarm);
        } catch (Exception e) {
            log.error("推送预警失败: alarmId={}", alarm.getId(), e);
        }
    }

    // ==================== 私有方法 ====================

    private AlarmVO convertToVO(Alarm alarm) {
        AlarmVO vo = new AlarmVO();
        BeanUtils.copyProperties(alarm, vo);

        // 设置报警类型名称
        AlarmTypeEnum typeEnum = AlarmTypeEnum.getByCode(alarm.getAlarmType());
        if (typeEnum != null) {
            vo.setAlarmTypeName(typeEnum.getName());
        }

        // 设置报警级别名称
        AlarmLevelEnum levelEnum = AlarmLevelEnum.getByCode(alarm.getAlarmLevel());
        if (levelEnum != null) {
            vo.setAlarmLevelName(levelEnum.getName());
        }

        // 设置处理状态名称
        AlarmHandleStatusEnum statusEnum = AlarmHandleStatusEnum.getByCode(alarm.getHandleStatus());
        if (statusEnum != null) {
            vo.setHandleStatusName(statusEnum.getName());
        }

        // 查询站点名称
        if (alarm.getSiteId() != null) {
            Site site = siteMapper.selectById(alarm.getSiteId());
            if (site != null) {
                vo.setSiteName(site.getSiteName());
            }
        }

        // 查询盒子名称
        if (alarm.getBoxId() != null) {
            Box box = boxMapper.selectById(alarm.getBoxId());
            if (box != null) {
                vo.setBoxName(box.getBoxName());
            }
        }

        // 查询通道名称
        if (alarm.getChannelId() != null) {
            Channel channel = channelMapper.selectById(alarm.getChannelId());
            if (channel != null) {
                vo.setChannelName(channel.getChannelName());
            }
        }

        return vo;
    }

    private AlarmDetailVO convertToDetailVO(Alarm alarm) {
        AlarmDetailVO vo = new AlarmDetailVO();
        BeanUtils.copyProperties(convertToVO(alarm), vo);

        // 处理人信息
        if (alarm.getHandleUserId() != null) {
            // TODO: 查询用户信息
            vo.setHandleUserName("未知用户");
        }

        return vo;
    }

}
