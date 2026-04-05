package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.common.enums.AlarmTypeEnum;
import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.device.domain.entity.Channel;
import com.vdc.pdi.device.domain.repository.ChannelRepository;
import com.vdc.pdi.ruleengine.domain.event.AlarmTriggerEvent;
import com.vdc.pdi.ruleengine.domain.event.StateStreamEvent;
import com.vdc.pdi.ruleengine.domain.event.TaskEndEvent;
import com.vdc.pdi.ruleengine.domain.event.TaskStartEvent;
import com.vdc.pdi.ruleengine.domain.vo.AlarmJudgeResult;
import com.vdc.pdi.ruleengine.domain.vo.ComplianceLevel;
import com.vdc.pdi.ruleengine.domain.vo.StateSnapshot;
import com.vdc.pdi.ruleengine.domain.vo.TaskDurationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 规则引擎服务
 * 核心规则匹配与事件处理
 */
@Service
@Slf4j
public class RuleEngineService {

    private final StateMachineManager stateMachineManager;
    private final StateSequenceMatcher sequenceMatcher;
    private final PdiTaskCalculator taskCalculator;
    private final AlarmJudge alarmJudge;
    private final ApplicationEventPublisher eventPublisher;
    private final ChannelRepository channelRepository;

    public RuleEngineService(StateMachineManager stateMachineManager,
                             StateSequenceMatcher sequenceMatcher,
                             PdiTaskCalculator taskCalculator,
                             AlarmJudge alarmJudge,
                             ApplicationEventPublisher eventPublisher,
                             ChannelRepository channelRepository) {
        this.stateMachineManager = stateMachineManager;
        this.sequenceMatcher = sequenceMatcher;
        this.taskCalculator = taskCalculator;
        this.alarmJudge = alarmJudge;
        this.eventPublisher = eventPublisher;
        this.channelRepository = channelRepository;
    }

    /**
     * 处理状态流事件（入口方法）
     * 由算法数据入口模块发布事件触发
     */
    @EventListener
    @Async("ruleEngineExecutor")
    public void onStateStreamEvent(StateStreamEvent event) {
        log.debug("接收状态流事件: channelId={}, siteId={}, stateCode={}",
                event.getChannelId(), event.getSiteId(), event.getStateCode());

        try {
            stateMachineManager.processStateStream(event);
        } catch (Exception e) {
            log.error("处理状态流事件失败: channelId={}, error={}",
                    event.getChannelId(), e.getMessage(), e);
        }
    }

    /**
     * 处理作业开始事件
     */
    @EventListener
    public void onTaskStart(TaskStartEvent event) {
        log.info("PDI作业开始: channelId={}, startTime={}, taskId={}",
                event.getChannelId(), event.getStartTime(), event.getTaskId());

        // 作业开始事件由pdi-behavior-archive模块监听处理
        // 这里可以添加额外的业务逻辑，如记录日志、发送通知等
    }

    /**
     * 处理作业结束事件
     */
    @EventListener
    @Async("ruleEngineExecutor")
    public void onTaskEnd(TaskEndEvent event) {
        log.info("PDI作业结束: channelId={}, endTime={}, taskId={}",
                event.getChannelId(), event.getEndTime(), event.getTaskId());

        try {
            // 获取通道位置信息
            String location = getChannelLocation(event.getChannelId());

            // 计算作业时长
            TaskDurationResult durationResult = taskCalculator.calculate(
                    event.getChannelId(),
                    event.getEntryTime(),
                    event.getEndTime()
            );

            log.info("PDI作业时长计算完成: channelId={}, actual={}, standard={}",
                    event.getChannelId(),
                    durationResult.getFormattedActualDuration(),
                    durationResult.getFormattedDeviation());

            // 判定合规性并生成报警详情
            AlarmJudgeResult judgeResult = alarmJudge.judgeWithDetails(
                    durationResult,
                    event.getChannelId(),
                    location
            );

            // 如果不合格，触发报警
            if (judgeResult.isNeedAlarm()) {
                publishAlarmEvent(event, durationResult, judgeResult.getDescription());
            }

            // 发布作业完成事件（包含合规判定结果）
            // 由pdi-behavior-archive模块监听并更新档案
            // 这里可以使用一个新的领域事件来传递完整信息

        } catch (Exception e) {
            log.error("处理作业结束事件失败: channelId={}, error={}",
                    event.getChannelId(), e.getMessage(), e);
        }
    }

    /**
     * 手动触发状态处理（用于测试或补偿）
     */
    public void manualProcessState(Long channelId, Long siteId, Integer stateCode) {
        log.info("手动触发状态处理: channelId={}, siteId={}, stateCode={}",
                channelId, siteId, stateCode);

        StateStreamEvent event = StateStreamEvent.of(
                this, channelId, siteId, stateCode,
                java.time.LocalDateTime.now(), null
        );
        stateMachineManager.processStateStream(event);
    }

    /**
     * 获取通道当前状态
     */
    public StateCodeEnum getCurrentState(Long channelId, Long siteId) {
        return stateMachineManager.getCurrentState(channelId, siteId);
    }

    /**
     * 获取通道状态历史
     */
    public List<StateSnapshot> getStateHistory(Long channelId, Long siteId, int limit) {
        return stateMachineManager.getStateHistory(channelId, siteId, limit);
    }

    /**
     * 重置通道状态机
     */
    public void resetStateMachine(Long channelId, Long siteId) {
        log.info("手动重置状态机: channelId={}, siteId={}", channelId, siteId);
        stateMachineManager.resetStateMachine(channelId, siteId);
    }

    /**
     * 判定作业合规性（供外部调用）
     */
    public ComplianceLevel judgeCompliance(TaskDurationResult durationResult) {
        return alarmJudge.judge(durationResult);
    }

    /**
     * 发布报警事件
     */
    private void publishAlarmEvent(TaskEndEvent event,
                                   TaskDurationResult durationResult,
                                   String description) {
        AlarmTriggerEvent alarmEvent = AlarmTriggerEvent.pdiViolation(
                this,
                event.getChannelId(),
                event.getSiteId(),
                event.getEndTime(),
                durationResult,
                description,
                event.getStateStreamId()
        );
        eventPublisher.publishEvent(alarmEvent);
        log.info("发布PDI违规报警事件: channelId={}, deviation={}",
                event.getChannelId(), durationResult.getFormattedDeviation());
    }

    /**
     * 获取通道位置信息
     */
    private String getChannelLocation(Long channelId) {
        try {
            return channelRepository.findById(channelId)
                    .map(ch -> ch.getName())
                    .orElse("未知通道");
        } catch (Exception e) {
            log.warn("获取通道信息失败: channelId={}", channelId);
            return "未知通道";
        }
    }
}
