package com.vdc.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GeneralConfigRequest {

    @NotBlank(message = "Config key is required")
    private String configKey;

    @NotBlank(message = "Config value is required")
    private String configValue;

    private String description;
}
