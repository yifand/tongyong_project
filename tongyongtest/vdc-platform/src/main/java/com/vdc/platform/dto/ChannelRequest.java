package com.vdc.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChannelRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    private String channelName;

    @NotNull(message = "Box ID is required")
    private Long boxId;

    private String channelType;

    private String algorithmType;

    private String rtspUrl;

    private String username;

    private String password;
}
