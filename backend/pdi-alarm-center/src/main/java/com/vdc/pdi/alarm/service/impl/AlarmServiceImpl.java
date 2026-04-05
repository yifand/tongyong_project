package com.vdc.pdi.alarm.service.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.BooleanBuilder;
import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import com.vdc.pdi.alarm.domain.entity.QAlarmRecord;
import com.vdc.pdi.alarm.domain.repository.AlarmQueryRepository;
import com.vdc.pdi.alarm.domain.repository.AlarmRepository;
import com.vdc.pdi.alarm.dto.request.AlarmHistoryRequest;
import com.vdc.pdi.alarm.dto.request.AlarmProcessRequest;
import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.dto.response.AlarmStatisticsResponse;
import com.vdc.pdi.alarm.mapper.AlarmMapper;
import com.vdc.pdi.alarm.service.AlarmCreateEvent;
import com.vdc.pdi.alarm.service.AlarmSSEService;
import com.vdc.pdi.alarm.service.AlarmService;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.common.enums.AlarmStatusEnum;
import com.vdc.pdi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 报警服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final AlarmQueryRepository alarmQueryRepository;
    private final AlarmMapper alarmMapper;
    private final AlarmSSEService alarmSSEService;

    @Override
    public List<AlarmResponse> getRealtimeAlarms(Integer limit, Integer type) {
        // 获取当前用户的数据权限范围（简化实现，实际需要根据权限系统获取）
        Set<Long> siteIds = getCurrentUserSiteIds();

        // 查询最近N条未删除的报警
        List<AlarmRecord> alarms = alarmQueryRepository.findRecentAlarms(siteIds, type, limit);

        return alarms.stream()
                .map(alarmMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<AlarmResponse> getHistoryAlarms(AlarmHistoryRequest request) {
        // 获取当前用户的数据权限范围
        Set<Long> siteIds = getCurrentUserSiteIds();

        // 构建查询条件
        Predicate predicate = buildHistoryPredicate(request, siteIds);

        // 执行分页查询
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "alarmTime")
        );

        Page<AlarmRecord> page = alarmQueryRepository.findAll(predicate, pageable);

        List<AlarmResponse> list = page.getContent().stream()
                .map(alarmMapper::toResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotalElements(), request.getPage(), request.getSize());
    }

    @Override
    public AlarmResponse getAlarmDetail(Long id) {
        AlarmRecord alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ALARM_NOT_FOUND", "报警记录不存在"));

        // 数据权限校验（简化实现）
        checkSiteAccess(alarm.getSiteId());

        return alarmMapper.toResponse(alarm);
    }

    @Override
    @Transactional
    public void processAlarm(Long id, AlarmProcessRequest request) {
        AlarmRecord alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ALARM_NOT_FOUND", "报警记录不存在"));

        // 数据权限校验
        checkSiteAccess(alarm.getSiteId());

        // 校验状态
        if (alarm.getStatus() != null && alarm.getStatus().equals(AlarmStatusEnum.PROCESSED.getCode())) {
            throw new BusinessException("ALARM_ALREADY_PROCESSED", "报警已处理，请勿重复操作");
        }

        // 更新状态
        alarm.setStatus(AlarmStatusEnum.PROCESSED.getCode());
        alarm.setProcessorId(getCurrentUserId());
        alarm.setProcessedAt(LocalDateTime.now());
        alarm.setRemark(request != null ? request.getRemark() : null);

        alarmRepository.save(alarm);

        log.info("报警已处理: alarmId={}, processor={}", id, getCurrentUserId());
    }

    @Override
    @Transactional
    public void markFalsePositive(Long id, AlarmProcessRequest request) {
        AlarmRecord alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ALARM_NOT_FOUND", "报警记录不存在"));

        // 数据权限校验
        checkSiteAccess(alarm.getSiteId());

        // 更新状态
        alarm.setStatus(AlarmStatusEnum.FALSE_POSITIVE.getCode());
        alarm.setProcessorId(getCurrentUserId());
        alarm.setProcessedAt(LocalDateTime.now());
        alarm.setRemark(request != null ? request.getRemark() : null);

        alarmRepository.save(alarm);

        log.info("报警标记为误报: alarmId={}, processor={}", id, getCurrentUserId());
    }

    @Override
    public AlarmStatisticsResponse getTodayStatistics() {
        // 获取当前用户的数据权限范围
        Set<Long> siteIds = getCurrentUserSiteIds();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        var statistics = alarmQueryRepository.getStatistics(
                siteIds, startOfDay, endOfDay
        );

        return alarmMapper.toStatisticsResponse(statistics);
    }

    @Override
    @Transactional
    public AlarmRecord createAlarm(AlarmCreateEvent event) {
        AlarmRecord alarm = new AlarmRecord();
        alarm.setType(event.getType());
        alarm.setSiteId(event.getSiteId());
        alarm.setChannelId(event.getChannelId());
        alarm.setAlarmTime(event.getAlarmTime());
        alarm.setLocation(event.getLocation());
        alarm.setFaceImageUrl(event.getFaceImageUrl());
        alarm.setSceneImageUrl(event.getSceneImageUrl());
        alarm.setStatus(AlarmStatusEnum.UNPROCESSED.getCode());
        alarm.setExtraInfo(event.getExtraInfo());

        AlarmRecord saved = alarmRepository.save(alarm);

        // SSE实时推送
        alarmSSEService.pushAlarm(alarmMapper.toResponse(saved));

        log.info("新报警创建: alarmId={}, type={}, siteId={}",
                saved.getId(), event.getType(), event.getSiteId());

        return saved;
    }

    private Predicate buildHistoryPredicate(AlarmHistoryRequest request, Set<Long> siteIds) {
        QAlarmRecord alarm = QAlarmRecord.alarmRecord;
        BooleanBuilder builder = new BooleanBuilder();

        // 数据权限条件
        builder.and(alarm.siteId.in(siteIds));

        // 站点筛选
        if (request.getSiteId() != null) {
            builder.and(alarm.siteId.eq(request.getSiteId()));
        }

        // 类型筛选
        if (request.getType() != null) {
            builder.and(alarm.type.eq(request.getType()));
        }

        // 通道筛选
        if (request.getChannelId() != null) {
            builder.and(alarm.channelId.eq(request.getChannelId()));
        }

        // 状态筛选
        if (request.getStatus() != null) {
            builder.and(alarm.status.eq(request.getStatus()));
        }

        // 时间范围筛选
        if (request.getStartTime() != null) {
            builder.and(alarm.alarmTime.goe(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            builder.and(alarm.alarmTime.lt(request.getEndTime()));
        }

        // 逻辑删除排除
        builder.and(alarm.deletedAt.isNull());

        return builder;
    }

    /**
     * 获取当前用户的站点权限（简化实现）
     */
    private Set<Long> getCurrentUserSiteIds() {
        // 实际实现中应从权限系统获取
        // 这里返回所有站点作为示例
        return Set.of(1L, 2L);
    }

    /**
     * 获取当前用户ID（简化实现）
     */
    private Long getCurrentUserId() {
        // 实际实现中应从安全上下文获取
        return 1L;
    }

    /**
     * 校验站点访问权限（简化实现）
     */
    private void checkSiteAccess(Long siteId) {
        Set<Long> siteIds = getCurrentUserSiteIds();
        if (!siteIds.contains(siteId)) {
            throw new BusinessException("FORBIDDEN", "无权限访问该站点数据");
        }
    }
}
