package com.vdc.pdi.systemconfig.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 配置分组响应DTO
 */
@Schema(description = "配置分组响应")
public class ConfigGroupResponse {

    @Schema(description = "配置分组")
    private String configGroup;

    @Schema(description = "分组描述")
    private String description;

    @Schema(description = "配置项列表")
    private List<ConfigResponse> configs;

    // Getters and Setters
    public String getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ConfigResponse> getConfigs() {
        return configs;
    }

    public void setConfigs(List<ConfigResponse> configs) {
        this.configs = configs;
    }
}
