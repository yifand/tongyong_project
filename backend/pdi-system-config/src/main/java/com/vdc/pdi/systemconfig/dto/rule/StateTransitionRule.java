package com.vdc.pdi.systemconfig.dto.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 状态转换规则配置
 * 供规则引擎调用
 */
@Data
@Schema(description = "状态转换规则配置")
public class StateTransitionRule {

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "状态转换定义列表")
    private List<StateTransition> transitions;

    @Schema(description = "是否启用")
    private Boolean enabled;

    /**
     * 状态转换定义
     */
    @Data
    @Schema(description = "状态转换定义")
    public static class StateTransition {

        @Schema(description = "源状态")
        private String fromState;

        @Schema(description = "目标状态")
        private String toState;

        @Schema(description = "触发条件")
        private String condition;

        @Schema(description = "转换描述")
        private String description;

        @Schema(description = "是否允许自动转换")
        private Boolean autoTransition;

        @Schema(description = "转换超时时间（秒）")
        private Integer timeoutSeconds;
    }
}
