package com.vdc.platform.ruleengine.core;

import com.vdc.platform.entity.Alarm;
import com.vdc.platform.entity.Channel;
import com.vdc.platform.entity.EdgeBox;
import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.entity.WorkSession;
import com.vdc.platform.ruleengine.model.RuleEvent;
import com.vdc.platform.service.IAlarmService;
import com.vdc.platform.service.IChannelService;
import com.vdc.platform.service.IEdgeBoxService;
import com.vdc.platform.service.IWorkSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RuleActionExecutor {

    private final IWorkSessionService workSessionService;
    private final IAlarmService alarmService;
    private final IChannelService channelService;
    private final IEdgeBoxService edgeBoxService;

    @Transactional
    public Long onEnter(RuleEvent event, RuleConfig config) {
        Long channelIdLong = resolveChannelId(event.getChannelId());
        Long siteId = resolveSiteId(event.getChannelId());

        WorkSession session = new WorkSession();
        session.setChannelId(channelIdLong);
        session.setSiteId(siteId);
        session.setStatus(0);
        session.setStartTime(event.getTimestamp());
        session.setStandardDuration(config.getStandardDuration());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        workSessionService.save(session);
        return session.getId();
    }

    @Transactional
    public void onExit(RuleEvent event, RuleConfig config) {
        Long channelIdLong = resolveChannelId(event.getChannelId());

        WorkSession session = workSessionService.lambdaQuery()
                .eq(WorkSession::getChannelId, channelIdLong)
                .orderByDesc(WorkSession::getStartTime)
                .last("LIMIT 1")
                .one();

        if (session == null) {
            return;
        }

        LocalDateTime endTime = event.getTimestamp();
        long actualSeconds = Duration.between(session.getStartTime(), endTime).getSeconds();
        int actualDuration = (int) Math.max(0, actualSeconds);

        BigDecimal deviationPct = BigDecimal.ZERO;
        if (config.getStandardDuration() != null && config.getStandardDuration() > 0) {
            deviationPct = BigDecimal.valueOf(actualDuration)
                    .subtract(BigDecimal.valueOf(config.getStandardDuration()))
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(config.getStandardDuration()), 2, RoundingMode.HALF_UP);
        }

        String result = calculateResult(actualDuration, config);

        session.setEndTime(endTime);
        session.setActualDuration(actualDuration);
        session.setDeviationPct(deviationPct);
        session.setResult(result);
        session.setStatus(1);
        session.setUpdatedAt(LocalDateTime.now());

        workSessionService.updateById(session);

        if ("UNQUALIFIED".equals(result)) {
            Alarm alarm = new Alarm();
            alarm.setAlarmType("PDI_UNQUALIFIED");
            alarm.setSiteId(session.getSiteId());
            alarm.setChannelId(channelIdLong);
            alarm.setWorkSessionId(session.getId());
            alarm.setAlarmTime(endTime);
            alarm.setProcessStatus("UNPROCESSED");
            alarm.setDescription("Work session duration below critical threshold");
            alarm.setCreatedAt(LocalDateTime.now());
            alarm.setUpdatedAt(LocalDateTime.now());
            alarmService.save(alarm);
        }
    }

    @Transactional
    public void onSmoke(RuleEvent event) {
        Long channelIdLong = resolveChannelId(event.getChannelId());
        Long siteId = resolveSiteId(event.getChannelId());

        Alarm alarm = new Alarm();
        alarm.setAlarmType("SMOKE");
        alarm.setSiteId(siteId);
        alarm.setChannelId(channelIdLong);
        alarm.setAlarmTime(event.getTimestamp());
        alarm.setProcessStatus("UNPROCESSED");
        alarm.setDescription(event.getDescription());
        alarm.setCreatedAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());
        alarmService.save(alarm);
    }

    @Transactional
    public void onViolation(RuleEvent event) {
        Long channelIdLong = resolveChannelId(event.getChannelId());
        Long siteId = resolveSiteId(event.getChannelId());

        Alarm alarm = new Alarm();
        alarm.setAlarmType("VIOLATION");
        alarm.setSiteId(siteId);
        alarm.setChannelId(channelIdLong);
        alarm.setWorkSessionId(event.getWorkSessionId());
        alarm.setAlarmTime(event.getTimestamp());
        alarm.setProcessStatus("UNPROCESSED");
        alarm.setDescription(event.getDescription());
        alarm.setCreatedAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());
        alarmService.save(alarm);
    }

    private String calculateResult(int actualDuration, RuleConfig config) {
        if (config.getStandardDuration() == null || config.getStandardDuration() <= 0) {
            return "QUALIFIED";
        }
        BigDecimal threshold = BigDecimal.valueOf(config.getStandardDuration())
                .multiply(config.getCriticalThresholdPct() != null ? config.getCriticalThresholdPct() : BigDecimal.valueOf(90))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (BigDecimal.valueOf(actualDuration).compareTo(threshold) < 0) {
            return "UNQUALIFIED";
        }
        if (BigDecimal.valueOf(actualDuration).compareTo(BigDecimal.valueOf(config.getStandardDuration())) < 0) {
            return "CRITICAL";
        }
        return "QUALIFIED";
    }

    private Long resolveChannelId(String channelIdStr) {
        Channel channel = channelService.lambdaQuery()
                .eq(Channel::getChannelId, channelIdStr)
                .last("LIMIT 1")
                .one();
        return channel != null ? channel.getId() : null;
    }

    private Long resolveSiteId(String channelIdStr) {
        Channel channel = channelService.lambdaQuery()
                .eq(Channel::getChannelId, channelIdStr)
                .last("LIMIT 1")
                .one();
        if (channel == null || channel.getBoxId() == null) {
            return null;
        }
        EdgeBox box = edgeBoxService.getById(channel.getBoxId());
        return box != null ? box.getSiteId() : null;
    }
}
