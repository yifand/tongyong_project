package com.vdc.pdi.behaviorarchive.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 状态流实体（用于获取截图）
 * 对应数据库表 state_stream
 */
@Entity
@Table(name = "state_stream", indexes = {
    @Index(name = "idx_state_channel_id", columnList = "channel_id"),
    @Index(name = "idx_state_event_time", columnList = "event_time"),
    @Index(name = "idx_state_code", columnList = "state_code")
})
public class StateStream extends BaseEntity {

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
     * 事件时间
     */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    /**
     * 门状态：0-关，1-开
     */
    @Column(name = "door_open")
    private Integer doorOpen;

    /**
     * 人员存在：0-无人，1-有人
     */
    @Column(name = "person_present")
    private Integer personPresent;

    /**
     * 进出状态：0-未进出，1-进出中
     */
    @Column(name = "entering_exiting")
    private Integer enteringExiting;

    /**
     * 状态码：S1=1, S3=3, S5=5, S7=7, S8=8
     */
    @Column(name = "state_code")
    private Integer stateCode;

    /**
     * 截图URL
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

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

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public Integer getDoorOpen() {
        return doorOpen;
    }

    public void setDoorOpen(Integer doorOpen) {
        this.doorOpen = doorOpen;
    }

    public Integer getPersonPresent() {
        return personPresent;
    }

    public void setPersonPresent(Integer personPresent) {
        this.personPresent = personPresent;
    }

    public Integer getEnteringExiting() {
        return enteringExiting;
    }

    public void setEnteringExiting(Integer enteringExiting) {
        this.enteringExiting = enteringExiting;
    }

    public Integer getStateCode() {
        return stateCode;
    }

    public void setStateCode(Integer stateCode) {
        this.stateCode = stateCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
