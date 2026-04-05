package com.vdc.pdi.systemconfig.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 算法配置响应DTO
 */
@Schema(description = "算法配置响应")
public class AlgorithmConfigResponse {

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "通道ID（全局配置为null）")
    private Long channelId;

    @Schema(description = "算法类型")
    private String algorithmType;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "灵敏度")
    private String sensitivity;

    @Schema(description = "连续触发帧数")
    private Integer triggerFrames;

    @Schema(description = "标准工时（分钟）")
    private Integer standardDuration;

    @Schema(description = "进出判定时间窗口（秒）")
    private Integer enterExitWindow;

    @Schema(description = "人员消失超时阈值（秒）")
    private Integer personDisappearTimeout;

    @Schema(description = "是否继承全局配置")
    private Boolean inheritGlobal;

    @Schema(description = "扩展参数JSON")
    private String extraParams;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
