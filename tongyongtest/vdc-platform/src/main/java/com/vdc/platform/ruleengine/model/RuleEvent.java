package com.vdc.platform.ruleengine.model;

import com.vdc.platform.ruleengine.enums.RuleEventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuleEvent {

    private RuleEventType eventType;
    private String channelId;
    private LocalDateTime timestamp;
    private Long workSessionId;
    private String description;
}
