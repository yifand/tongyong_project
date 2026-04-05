package com.vdc.pdi.systemconfig.dto.request;

import com.vdc.pdi.systemconfig.domain.entity.BusinessRule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 业务规则请求DTO
 */
@Schema(description = "业务规则请求")
public class BusinessRuleRequest {

    @Schema(description = "规则名称")
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100")
    private String ruleName;

    @Schema(description = "规则配置JSON")
    @NotBlank(message = "规则配置不能为空")
    private String ruleConfig;

    @Schema(description = "是否启用")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "规则描述")
    @Size(max = 500, message = "规则描述长度不能超过500")
    private String description;

    // Getters and Setters
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(String ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
