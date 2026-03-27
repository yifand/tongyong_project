package com.pdi.receiver.statemachine;

import com.pdi.common.enums.PDITaskStatusEnum;
import com.pdi.dao.entity.PDITask;
import com.pdi.dao.mapper.PDITaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PDI任务管理器
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component
public class PDITaskManager {

    /**
     * 当前正在进行的PDI任务缓存 (channelId -> taskId)
     */
    private final Map<Long, Long> activeTasks = new ConcurrentHashMap<>();

    /**
     * 通道当前状态缓存 (channelId -> stateCode)
     */
    private final Map<Long, String> channelStates = new ConcurrentHashMap<>();

    @Autowired
    private PDITaskMapper pdiTaskMapper;

    /**
     * 开始PDI任务
     *
     * @param channelId 通道ID
     * @param boxId     盒子ID
     * @param siteId    站点ID
     * @return 任务ID
     */
    public Long startTask(Long channelId, Long boxId, Long siteId) {
        // 检查是否已有进行中的任务
        Long existingTaskId = activeTasks.get(channelId);
        if (existingTaskId != null) {
            log.warn("通道{}已有进行中的任务{}，将创建新任务", channelId, existingTaskId);
        }

        // 生成任务编号
        String taskNo = generateTaskNo(channelId);

        // 创建任务
        PDITask task = new PDITask();
        task.setTaskNo(taskNo);
        task.setChannelId(channelId);
        task.setBoxId(boxId);
        task.setSiteId(siteId);
        task.setStartTime(LocalDateTime.now());
        task.setTaskStatus(PDITaskStatusEnum.IN_PROGRESS.getCode());
        task.setStandardDuration(1800); // 默认30分钟

        pdiTaskMapper.insert(task);

        // 缓存活动任务
        activeTasks.put(channelId, task.getId());
        log.info("PDI任务已创建: channelId={}, taskId={}, taskNo={}", channelId, task.getId(), taskNo);

        return task.getId();
    }

    /**
     * 完成PDI任务
     *
     * @param channelId 通道ID
     * @param taskId    任务ID
     */
    public void completeTask(Long channelId, Long taskId) {
        PDITask task = pdiTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }

        LocalDateTime endTime = LocalDateTime.now();
        task.setEndTime(endTime);

        // 计算作业时长(秒)
        if (task.getStartTime() != null) {
            long seconds = java.time.Duration.between(task.getStartTime(), endTime).getSeconds();
            task.setDurationSeconds((int) seconds);
        }

        task.setTaskStatus(PDITaskStatusEnum.COMPLETED.getCode());
        pdiTaskMapper.updateById(task);

        // 从缓存中移除
        activeTasks.remove(channelId);
        log.info("PDI任务已完成: channelId={}, taskId={}, duration={}秒", 
                channelId, taskId, task.getDurationSeconds());
    }

    /**
     * 异常中断PDI任务
     *
     * @param channelId 通道ID
     * @param taskId    任务ID
     * @param reason    中断原因
     */
    public void interruptTask(Long channelId, Long taskId, String reason) {
        PDITask task = pdiTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }

        task.setEndTime(LocalDateTime.now());
        task.setTaskStatus(PDITaskStatusEnum.INTERRUPTED.getCode());
        task.setRemark(reason);
        pdiTaskMapper.updateById(task);

        // 从缓存中移除
        activeTasks.remove(channelId);
        log.info("PDI任务已中断: channelId={}, taskId={}, reason={}", channelId, taskId, reason);
    }

    /**
     * 获取当前任务
     *
     * @param channelId 通道ID
     * @return 任务ID
     */
    public Long getCurrentTask(Long channelId) {
        return activeTasks.get(channelId);
    }

    /**
     * 检查是否有进行中的任务
     *
     * @param channelId 通道ID
     * @return true-有进行中的任务
     */
    public boolean hasActiveTask(Long channelId) {
        return activeTasks.containsKey(channelId);
    }

    /**
     * 更新通道状态
     *
     * @param channelId 通道ID
     * @param stateCode 状态码
     */
    public void updateChannelState(Long channelId, String stateCode) {
        channelStates.put(channelId, stateCode);
    }

    /**
     * 获取通道当前状态
     *
     * @param channelId 通道ID
     * @return 状态码
     */
    public String getChannelState(Long channelId) {
        return channelStates.getOrDefault(channelId, PDIState.S1.getCode());
    }

    /**
     * 清除通道状态
     *
     * @param channelId 通道ID
     */
    public void clearChannelState(Long channelId) {
        channelStates.remove(channelId);
        activeTasks.remove(channelId);
    }

    /**
     * 生成任务编号
     *
     * @param channelId 通道ID
     * @return 任务编号
     */
    private String generateTaskNo(Long channelId) {
        return "PDI" + channelId + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
