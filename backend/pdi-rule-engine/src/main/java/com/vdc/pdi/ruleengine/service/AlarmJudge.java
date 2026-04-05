package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.common.enums.AlarmTypeEnum;
import com.vdc.pdi.ruleengine.domain.vo.AlarmJudgeResult;
import com.vdc.pdi.ruleengine.domain.vo.ComplianceLevel;
import com.vdc.pdi.ruleengine.domain.vo.TaskDurationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 报警判定器
 * 根据作业时长判定合规等级
 */
@Component
@Slf4j
public class AlarmJudge {

    /**
     * 临界阈值比例 (0.9 = 90%)
     */
    @Value("${rule-engine.alarm.critical-ratio:0.9}")
    private double criticalRatio;

    /**
     * 判定合规等级
     *
     * <p>判定规则:
     * <ul>
     *   <li>duration >= standard: 合格 (PASSED)</li>
     *   <li>duration >= 0.9 * standard: 临界 (CRITICAL)</li>
     *   <li>duration < 0.9 * standard: 不合格/报警 (FAILED)</li>
     * </ul>
     *
     * @param durationResult 作业时长结果
     * @return 合规等级
     */
    public ComplianceLevel judge(TaskDurationResult durationResult) {
        long actual = durationResult.getActualSeconds();
        long standard = durationResult.getStandardSeconds();
        long criticalThreshold = (long) (standard * criticalRatio);

        if (actual >= standard) {
            log.debug("作业时长合格: channelId={}, actual={}, standard={}",
                    durationResult.getChannelId(), actual, standard);
            return ComplianceLevel.PASSED;
        } else if (actual >= criticalThreshold) {
            log.debug("作业时长临界: channelId={}, actual={}, criticalThreshold={}",
                    durationResult.getChannelId(), actual, criticalThreshold);
            return ComplianceLevel.CRITICAL;
        } else {
            log.info("作业时长不合格: channelId={}, actual={}, standard={}, deviation={}",
                    durationResult.getChannelId(), actual, standard,
                    durationResult.getDeviationSeconds());
            return ComplianceLevel.FAILED;
        }
    }

    /**
     * 判定并生成报警详情
     *
     * @param durationResult 作业时长结果
     * @param channelId      通道ID
     * @param location       地点描述
     * @return 报警判定结果
     */
    public AlarmJudgeResult judgeWithDetails(TaskDurationResult durationResult,
                                              Long channelId,
                                              String location) {
        ComplianceLevel level = judge(durationResult);

        String description = generateDescription(durationResult, level, location);

        return AlarmJudgeResult.builder()
                .complianceLevel(level)
                .needAlarm(level == ComplianceLevel.FAILED)
                .alarmType(AlarmTypeEnum.PDI_VIOLATION)
                .description(description)
                .durationResult(durationResult)
                .build();
    }

    /**
     * 判定是否需要报警
     *
     * @param durationResult 作业时长结果
     * @return true如果需要报警
     */
    public boolean needAlarm(TaskDurationResult durationResult) {
        return judge(durationResult) == ComplianceLevel.FAILED;
    }

    private String generateDescription(TaskDurationResult result,
                                       ComplianceLevel level,
                                       String location) {
        String loc = location != null ? location : "未知位置";

        return switch (level) {
            case PASSED -> String.format("%s PDI作业达标，时长: %s (标准: %s)",
                    loc,
                    result.getFormattedActualDuration(),
                    formatDuration(result.getStandardSeconds()));
            case CRITICAL -> String.format("%s PDI作业临界，时长: %s (标准: %s, 偏差: %s)",
                    loc,
                    result.getFormattedActualDuration(),
                    formatDuration(result.getStandardSeconds()),
                    result.getFormattedDeviation());
            case FAILED -> String.format("%s PDI作业未达标，时长: %s (标准: %s, 偏差: %s)",
                    loc,
                    result.getFormattedActualDuration(),
                    formatDuration(result.getStandardSeconds()),
                    result.getFormattedDeviation());
        };
    }

    private String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d分%d秒", minutes, secs);
    }
}
