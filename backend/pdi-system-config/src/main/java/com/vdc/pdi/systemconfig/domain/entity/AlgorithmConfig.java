package com.vdc.pdi.systemconfig.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 算法配置实体
 * 存储通道或全局算法配置参数
 */
@Entity
@Table(name = "algorithm_config", indexes = {
    @Index(name = "idx_algorithm_channel", columnList = "channel_id"),
    @Index(name = "idx_algorithm_type", columnList = "algorithm_type"),
    @Index(name = "idx_algorithm_site", columnList = "site_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_algorithm_config", columnNames = {"site_id", "channel_id", "algorithm_type"})
})
public class AlgorithmConfig extends BaseEntity {

    /**
     * 通道ID，NULL表示全局默认配置
     */
    @Column(name = "channel_id")
    private Long channelId;

    /**
     * 算法类型: SMOKE-抽烟检测, PDI_LEFT_FRONT-左前门, PDI_LEFT_REAR-左后门, PDI_SLIDE-滑移门
     */
    @Column(name = "algorithm_type", nullable = false, length = 50)
    private String algorithmType;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 灵敏度: LOW-低, MEDIUM-中, HIGH-高
     */
    @Column(name = "sensitivity", length = 20)
    private String sensitivity = "MEDIUM";

    /**
     * 连续触发帧数（防误报）
     */
    @Column(name = "trigger_frames")
    private Integer triggerFrames = 3;

    /**
     * 标准工时（分钟），PDI算法专用
     */
    @Column(name = "standard_duration")
    private Integer standardDuration;

    /**
     * 进出判定时间窗口（秒）
     */
    @Column(name = "enter_exit_window")
    private Integer enterExitWindow = 5;

    /**
     * 人员消失超时阈值（秒）
     */
    @Column(name = "person_disappear_timeout")
    private Integer personDisappearTimeout = 10;

    /**
     * 是否继承全局配置
     */
    @Column(name = "inherit_global", nullable = false)
    private Boolean inheritGlobal = true;

    /**
     * 扩展参数（JSON格式）
     */
    @Column(name = "extra_params", columnDefinition = "TEXT")
    private String extraParams;

    // Getters and Setters
    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    public Integer getTriggerFrames() {
        return triggerFrames;
    }

    public void setTriggerFrames(Integer triggerFrames) {
        this.triggerFrames = triggerFrames;
    }

    public Integer getStandardDuration() {
        return standardDuration;
    }

    public void setStandardDuration(Integer standardDuration) {
        this.standardDuration = standardDuration;
    }

    public Integer getEnterExitWindow() {
        return enterExitWindow;
    }

    public void setEnterExitWindow(Integer enterExitWindow) {
        this.enterExitWindow = enterExitWindow;
    }

    public Integer getPersonDisappearTimeout() {
        return personDisappearTimeout;
    }

    public void setPersonDisappearTimeout(Integer personDisappearTimeout) {
        this.personDisappearTimeout = personDisappearTimeout;
    }

    public Boolean getInheritGlobal() {
        return inheritGlobal;
    }

    public void setInheritGlobal(Boolean inheritGlobal) {
        this.inheritGlobal = inheritGlobal;
    }

    public String getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }
}
