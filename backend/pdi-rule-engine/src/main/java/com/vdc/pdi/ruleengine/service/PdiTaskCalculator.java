package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.ruleengine.domain.vo.SingleTaskInfo;
import com.vdc.pdi.ruleengine.domain.vo.TaskDurationResult;
import com.vdc.pdi.ruleengine.exception.RuleEngineException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * PDI作业时长计算器
 * 支持单通道时长计算和多车门合并计算
 */
@Component
@Slf4j
public class PdiTaskCalculator {

    // 默认标准工时（分钟）
    private static final int DEFAULT_STANDARD_MINUTES = 12;
    private static final int DEFAULT_VEHICLE_MINUTES = 15;

    @Value("${rule-engine.pdi.standard-minutes:12}")
    private int standardMinutes;

    @Value("${rule-engine.pdi.vehicle-standard-minutes:15}")
    private int vehicleStandardMinutes;

    /**
     * 计算单通道作业时长
     *
     * @param channelId 通道ID
     * @param entryTime 进入时间
     * @param exitTime  离开时间
     * @return 作业时长结果
     */
    public TaskDurationResult calculate(Long channelId,
                                        LocalDateTime entryTime,
                                        LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            throw new RuleEngineException("进入时间和离开时间不能为空");
        }

        if (exitTime.isBefore(entryTime)) {
            throw new RuleEngineException("离开时间不能早于进入时间");
        }

        Duration duration = Duration.between(entryTime, exitTime);
        long seconds = duration.getSeconds();

        // 使用配置的标准工时
        long standardSeconds = standardMinutes * 60L;

        // 计算偏差
        long deviationSeconds = seconds - standardSeconds;

        log.debug("单通道作业时长计算: channelId={}, actual={}s, standard={}s, deviation={}s",
                channelId, seconds, standardSeconds, deviationSeconds);

        return TaskDurationResult.builder()
                .channelId(channelId)
                .merged(false)
                .entryTime(entryTime)
                .exitTime(exitTime)
                .actualSeconds(seconds)
                .standardSeconds(standardSeconds)
                .deviationSeconds(deviationSeconds)
                .build();
    }

    /**
     * 多车门合并计算
     * 总时长 = max(T4, T5, T6) - min(T1, T2, T3)
     *
     * @param tasks 各车门作业记录
     * @return 合并后的时长结果
     */
    public TaskDurationResult calculateMerged(List<SingleTaskInfo> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new RuleEngineException("任务列表不能为空");
        }

        // 验证所有任务都有时间数据
        for (SingleTaskInfo task : tasks) {
            if (task.getEntryTime() == null || task.getExitTime() == null) {
                throw new RuleEngineException("任务进入时间和离开时间不能为空: channelId=" + task.getChannelId());
            }
        }

        // 找出最早开始时间和最晚结束时间
        LocalDateTime earliestStart = tasks.stream()
                .map(SingleTaskInfo::getEntryTime)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new RuleEngineException("无法获取最早开始时间"));

        LocalDateTime latestEnd = tasks.stream()
                .map(SingleTaskInfo::getExitTime)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new RuleEngineException("无法获取最晚结束时间"));

        if (latestEnd.isBefore(earliestStart)) {
            throw new RuleEngineException("最晚结束时间不能早于最早开始时间");
        }

        // 计算总时长
        Duration totalDuration = Duration.between(earliestStart, latestEnd);
        long totalSeconds = totalDuration.getSeconds();

        // 使用车辆PDI标准工时
        long standardSeconds = vehicleStandardMinutes * 60L;

        // 计算偏差
        long deviationSeconds = totalSeconds - standardSeconds;

        log.debug("多车门合并计算: taskCount={}, earliestStart={}, latestEnd={}, total={}s, standard={}s",
                tasks.size(), earliestStart, latestEnd, totalSeconds, standardSeconds);

        return TaskDurationResult.builder()
                .merged(true)
                .subTasks(tasks)
                .entryTime(earliestStart)
                .exitTime(latestEnd)
                .actualSeconds(totalSeconds)
                .standardSeconds(standardSeconds)
                .deviationSeconds(deviationSeconds)
                .build();
    }

    /**
     * 计算偏差百分比
     */
    public double calculateDeviationPercentage(TaskDurationResult result) {
        if (result.getStandardSeconds() == 0) {
            return 0.0;
        }
        return (double) result.getDeviationSeconds() / result.getStandardSeconds() * 100;
    }
}
