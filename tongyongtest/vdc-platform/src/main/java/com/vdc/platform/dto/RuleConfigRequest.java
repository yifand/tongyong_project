package com.vdc.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RuleConfigRequest {

    @NotBlank(message = "Rule name is required")
    private String ruleName;

    @NotBlank(message = "Channel type is required")
    private String channelType;

    @NotNull(message = "Require vehicle is required")
    private Boolean requireVehicle;

    @NotNull(message = "Enter pattern is required")
    private List<Object> enterPattern;

    @NotNull(message = "Exit pattern is required")
    private List<Object> exitPattern;

    @NotNull(message = "Standard duration is required")
    private Integer standardDuration;

    @NotNull(message = "Critical threshold is required")
    private BigDecimal criticalThresholdPct;

    private Integer personAbsentTimeout;

    @NotNull(message = "Enabled is required")
    private Boolean isEnabled;
}
