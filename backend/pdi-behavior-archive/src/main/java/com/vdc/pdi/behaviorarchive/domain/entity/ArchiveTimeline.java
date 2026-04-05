package com.vdc.pdi.behaviorarchive.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 档案时间线实体
 * 对应数据库表 archive_timeline
 */
@Entity
@Table(name = "archive_timeline", indexes = {
    @Index(name = "idx_timeline_archive_id", columnList = "archive_id"),
    @Index(name = "idx_timeline_seq", columnList = "archive_id, seq", unique = true)
})
public class ArchiveTimeline extends BaseEntity {

    /**
     * 关联的档案ID
     */
    @Column(name = "archive_id", nullable = false)
    private Long archiveId;

    /**
     * 事件时间
     */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    /**
     * 行为描述
     */
    @Column(name = "action", length = 100)
    private String action;

    /**
     * 截图URL
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 顺序号，用于排序
     */
    @Column(name = "seq", nullable = false)
    private Integer seq;

    // Getters and Setters
    public Long getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(Long archiveId) {
        this.archiveId = archiveId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }
}
