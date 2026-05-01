package com.vdc.platform.gateway.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class BoxHeartbeatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String boxId;
    private Long timestamp;
    private String status;
    private Map<String, Object> systemInfo;
    private List<ChannelStatus> channels;

    @Data
    public static class ChannelStatus implements Serializable {
        private static final long serialVersionUID = 1L;
        private String channelId;
        private String status;
    }
}
