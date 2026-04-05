package com.vdc.pdi.behaviorarchive.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 行为档案实体
 * 对应数据库表 behavior_archive
 */
@Entity
@Table(name = "behavior_archive", indexes = {
    @Index(name = "idx_archive_site_id", columnList = "site_id"),
    @Index(name = "idx_archive_status", columnList = "status"),
    @Index(name = "idx_archive_start_time", columnList = "start_time"),
    @Index(name = "idx_archive_pdi_task_id", columnList = "pdi_task_id", unique = true)
})
public class BehaviorArchive extends BaseEntity {

    /**
     * 关联的PDI任务ID
     */
    @Column(name = "pdi_task_id", nullable = false)
    private Long pdiTaskId;

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
     * 开始时间（人员进入时刻）
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 结束时间（人员离开时刻），进行中为NULL
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 预估检查时间（分钟）
     */
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    /**
     * 实际检查时间（秒）
     */
    @Column(name = "actual_duration")
    private Integer actualDuration;

    /**
     * 状态：0-进行中，1-达标，2-未达标
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    // Getters and Setters
    public Long getPdiTaskId() {
        return pdiTaskId;
    }

    public void setPdiTaskId(Long pdiTaskId) {
        this.pdiTaskId = pdiTaskId;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 是否进行中
     */
    @Transient
    public boolean isInProgress() {
        return this.status != null && this.status == 0;
    }

    /**
     * 是否已完成
     */
    @Transient
    public boolean isCompleted() {
        return this.status != null && this.status != 0;
    }
}
