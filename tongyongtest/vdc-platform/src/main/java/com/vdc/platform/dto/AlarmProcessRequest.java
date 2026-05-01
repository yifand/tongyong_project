package com.vdc.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlarmProcessRequest {

    @NotBlank(message = "Process status is required")
    private String processStatus;

    private String description;
}
