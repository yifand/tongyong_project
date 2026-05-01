package com.vdc.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vdc.platform.entity.Channel;
import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.entity.WorkSession;
import com.vdc.platform.mapper.WorkSessionMapper;
import com.vdc.platform.service.IChannelService;
import com.vdc.platform.service.IRuleConfigService;
import com.vdc.platform.service.IWorkSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WorkSessionServiceImpl extends ServiceImpl<WorkSessionMapper, WorkSession> implements IWorkSessionService {

    private final IRuleConfigService ruleConfigService;
    private final IChannelService channelService;

    @Override
    public WorkSession calculateMetrics(WorkSession session) {
        if (session == null) {
            return null;
        }

        LocalDateTime startTime = session.getStartTime();
        LocalDateTime endTime = session.getEndTime();

        if (startTime != null && endTime != null) {
            long actualSeconds = Duration.between(startTime, endTime).getSeconds();
            int actualDuration = (int) Math.max(0, actualSeconds);
            session.setActualDuration(actualDuration);

            Integer standardDuration = session.getStandardDuration();
            BigDecimal criticalThresholdPct = BigDecimal.valueOf(90);
            RuleConfig config = resolveRuleConfig(session.getChannelId());

            if (config != null) {
                if (standardDuration == null || standardDuration <= 0) {
                    standardDuration = config.getStandardDuration();
                    session.setStandardDuration(standardDuration);
                }
                if (config.getCriticalThresholdPct() != null) {
                    criticalThresholdPct = config.getCriticalThresholdPct();
                }
            }

            BigDecimal deviationPct = BigDecimal.ZERO;
            if (standardDuration != null && standardDuration > 0) {
                deviationPct = BigDecimal.valueOf(actualDuration)
                        .subtract(BigDecimal.valueOf(standardDuration))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(standardDuration), 2, RoundingMode.HALF_UP);
            }
            session.setDeviationPct(deviationPct);

            String result = calculateResult(actualDuration, standardDuration, criticalThresholdPct);
            session.setResult(result);
        }

        return session;
    }

    private String calculateResult(Integer actualDuration, Integer standardDuration, BigDecimal criticalThresholdPct) {
        if (actualDuration == null || standardDuration == null || standardDuration <= 0) {
            return "QUALIFIED";
        }

        BigDecimal threshold = BigDecimal.valueOf(standardDuration)
                .multiply(criticalThresholdPct != null ? criticalThresholdPct : BigDecimal.valueOf(90))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (BigDecimal.valueOf(actualDuration).compareTo(threshold) < 0) {
            return "UNQUALIFIED";
        }
        if (BigDecimal.valueOf(actualDuration).compareTo(BigDecimal.valueOf(standardDuration)) < 0) {
            return "CRITICAL";
        }
        return "QUALIFIED";
    }

    private RuleConfig resolveRuleConfig(Long channelId) {
        if (channelId == null) {
            return null;
        }
        Channel channel = channelService.getById(channelId);
        if (channel == null || channel.getChannelType() == null) {
            return null;
        }
        return ruleConfigService.lambdaQuery()
                .eq(RuleConfig::getChannelType, channel.getChannelType())
                .last("LIMIT 1")
                .one();
    }
}
