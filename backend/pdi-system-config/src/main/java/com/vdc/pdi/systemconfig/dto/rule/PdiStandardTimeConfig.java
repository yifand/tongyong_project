package com.vdc.pdi.systemconfig.dto.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * PDI标准工时配置
 * 供规则引擎调用
 */
@Data
@Schema(description = "PDI标准工时配置")
public class PdiStandardTimeConfig {

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "默认标准工时（秒）")
    private Integer defaultStandardDuration;

    @Schema(description = "通道特定配置列表")
    private List<ChannelConfig> channelConfigs;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "超时告警阈值（百分比，如120表示超时20%）")
    private Integer overtimeThresholdPercent;

    /**
     * 通道特定配置
     */
    @Data
    @Schema(description = "通道特定配置")
    public static class ChannelConfig {

        @Schema(description = "通道ID")
        private Long channelId;

        @Schema(description = "标准工时（秒）")
        private Integer standardDuration;

        @Schema(description = "是否启用自定义配置")
        private Boolean enabled;
    }
}
