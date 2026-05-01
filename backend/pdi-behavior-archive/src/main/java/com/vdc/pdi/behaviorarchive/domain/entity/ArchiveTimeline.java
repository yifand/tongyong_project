package com.vdc.pdi.behaviorarchive.domain.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 档案时间线实体
 * 对应数据库表 archive_timeline
 * 注意：不继承BaseEntity，因为时间线表不需要site_id字段（通过archive_id关联获取）
 */
@Entity
@Table(name = "archive_timeline", indexes = {
    @Index(name = "idx_timeline_archive_id", columnList = "archive_id"),
    @Index(name = "idx_timeline_seq", columnList = "archive_id, seq", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class ArchiveTimeline {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除时间，用于逻辑删除
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * 逻辑删除标记
     * @return true if deleted
     */
    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
