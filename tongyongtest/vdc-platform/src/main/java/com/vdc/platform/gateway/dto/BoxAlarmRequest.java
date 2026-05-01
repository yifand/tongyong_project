package com.vdc.platform.gateway.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class BoxAlarmRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String boxId;
    private Long timestamp;
    private String channelId;
    private String eventType;
    private Map<String, Object> eventDetectInfo;
    private Snapshot snapshot;

    @Data
    public static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        private String target;
        private String scene;
    }
}
