package com.vdc.pdi.behaviorarchive.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * PDI任务实体（用于关联查询）
 * 对应数据库表 pdi_task
 */
@Entity
@Table(name = "pdi_task")
public class PdiTask extends BaseEntity {

    /**
     * 通道ID
     */
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    /**
     * 站点ID
     */
    @Column(name = "site_id", nullable = false)
    private Long siteId;

    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 标准工时（分钟）
     */
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    /**
     * 实际时长（秒）
     */
    @Column(name = "actual_duration")
    private Integer actualDuration;

    /**
     * 结果：0-进行中，1-达标，2-未达标
     */
    @Column(name = "result")
    private Integer result;

    // Getters and Setters
    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Integer getActualDuration() {
        return actualDuration;
    }

    public void setActualDuration(Integer actualDuration) {
        this.actualDuration = actualDuration;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    /**
     * 是否已完成
     */
    @Transient
    public boolean isCompleted() {
        return this.endTime != null;
    }
}
