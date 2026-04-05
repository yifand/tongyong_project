package com.vdc.pdi.systemconfig.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 算法配置请求DTO
 */
@Schema(description = "算法配置请求")
public class AlgorithmConfigRequest {

    @Schema(description = "算法类型: SMOKE-抽烟检测, PDI_LEFT_FRONT-左前门, PDI_LEFT_REAR-左后门, PDI_SLIDE-滑移门")
    @NotNull(message = "算法类型不能为空")
    @Size(max = 50, message = "算法类型长度不能超过50")
    private String algorithmType;

    @Schema(description = "是否启用")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "灵敏度: LOW-低, MEDIUM-中, HIGH-高")
    @Size(max = 20, message = "灵敏度长度不能超过20")
    private String sensitivity;

    @Schema(description = "连续触发帧数（防误报）")
    @Min(value = 1, message = "触发帧数至少为1")
    @Max(value = 10, message = "触发帧数最大为10")
    private Integer triggerFrames;

    @Schema(description = "标准工时（分钟），PDI算法专用")
    @Min(value = 1, message = "标准工时至少为1分钟")
    @Max(value = 120, message = "标准工时最大为120分钟")
    private Integer standardDuration;

    @Schema(description = "进出判定时间窗口（秒）")
    @Min(value = 1, message = "时间窗口至少为1秒")
    @Max(value = 30, message = "时间窗口最大为30秒")
    private Integer enterExitWindow;

    @Schema(description = "人员消失超时阈值（秒）")
    @Min(value = 1, message = "超时阈值至少为1秒")
    @Max(value = 60, message = "超时阈值最大为60秒")
    private Integer personDisappearTimeout;

    @Schema(description = "是否继承全局配置")
    private Boolean inheritGlobal;

    @Schema(description = "扩展参数JSON")
    private String extraParams;

    // Getters and Setters
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
