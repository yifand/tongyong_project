package com.vdc.platform.gateway.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BoxStateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String boxId;
    private Long timestamp;
    private String channelId;
    private States states;
    private Snapshot snapshot;

    @Data
    public static class States implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean vehiclePresent;
        private Boolean doorOpen;
        private Boolean personPresent;
        private Boolean personEnteringExiting;
    }

    @Data
    public static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        private String target;
        private String scene;
    }
}
