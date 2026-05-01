package com.vdc.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ThresholdConfigRequest {

    @NotNull(message = "Rule ID is required")
    private Long id;

    @NotNull(message = "Standard duration is required")
    private Integer standardDuration;

    @NotNull(message = "Critical threshold is required")
    private BigDecimal criticalThresholdPct;
}
