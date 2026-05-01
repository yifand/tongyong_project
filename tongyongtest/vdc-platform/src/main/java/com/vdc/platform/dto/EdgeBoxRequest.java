package com.vdc.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EdgeBoxRequest {

    @NotBlank(message = "Box ID is required")
    private String boxId;

    private String boxName;

    @NotNull(message = "Site ID is required")
    private Long siteId;

    private String ipAddress;

    private String version;
}
