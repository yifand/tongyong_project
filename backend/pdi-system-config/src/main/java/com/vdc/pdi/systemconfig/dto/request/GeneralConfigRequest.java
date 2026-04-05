package com.vdc.pdi.systemconfig.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 通用配置请求DTO
 * 支持批量更新同一分组下的多个配置项
 */
@Schema(description = "通用配置请求")
public class GeneralConfigRequest {

    @Schema(description = "配置分组")
    @NotBlank(message = "配置分组不能为空")
    private String configGroup;

    @Schema(description = "配置项列表")
    @NotEmpty(message = "配置项不能为空")
    @Valid
    private List<ConfigItem> configs;

    /**
     * 配置项
     */
    @Schema(description = "配置项")
    public static class ConfigItem {

        @Schema(description = "配置键")
        @NotBlank(message = "配置键不能为空")
        private String configKey;

        @Schema(description = "配置值")
        private String configValue;

        // Getters and Setters
        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigValue() {
            return configValue;
        }

        public void setConfigValue(String configValue) {
            this.configValue = configValue;
        }
    }

    // Getters and Setters
    public String getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }

    public List<ConfigItem> getConfigs() {
        return configs;
    }

    public void setConfigs(List<ConfigItem> configs) {
        this.configs = configs;
    }
}
