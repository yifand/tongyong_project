package com.vdc.pdi.systemconfig.dto.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 告警阈值配置
 * 供规则引擎调用
 */
@Data
@Schema(description = "告警阈值配置")
public class AlarmThresholdConfig {

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "告警级别阈值配置")
    private List<AlarmLevelThreshold> levelThresholds;

    @Schema(description = "告警类型阈值配置")
    private List<AlarmTypeThreshold> typeThresholds;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "抑制时间（秒，相同告警的抑制周期）")
    private Integer suppressSeconds;

    @Schema(description = "最大告警次数（0表示不限制）")
    private Integer maxAlarmCount;

    /**
     * 告警级别阈值
     */
    @Data
    @Schema(description = "告警级别阈值")
    public static class AlarmLevelThreshold {

        @Schema(description = "告警级别（INFO/WARNING/ERROR/CRITICAL）")
        private String level;

        @Schema(description = "持续时间阈值（秒）")
        private Integer durationThreshold;

        @Schema(description = "触发次数阈值")
        private Integer countThreshold;

        @Schema(description = "升级时间（秒，超过后升级到更高级别）")
        private Integer escalationSeconds;
    }

    /**
     * 告警类型阈值
     */
    @Data
    @Schema(description = "告警类型阈值")
    public static class AlarmTypeThreshold {

        @Schema(description = "告警类型编码")
        private String alarmType;

        @Schema(description = "告警类型名称")
        private String alarmName;

        @Schema(description = "置信度阈值（0-100）")
        private Integer confidenceThreshold;

        @Schema(description = "连续触发帧数")
        private Integer triggerFrames;

        @Schema(description = "冷却时间（秒）")
        private Integer cooldownSeconds;
    }
}
