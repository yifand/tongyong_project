package com.vdc.platform.ruleengine.core;

import com.vdc.platform.ruleengine.model.RuleEvent;
import org.springframework.stereotype.Component;

@Component
public class EventGenerator {

    public RuleEvent fromMatch(RuleEvent matchedEvent) {
        if (matchedEvent == null) {
            return null;
        }
        RuleEvent event = new RuleEvent();
        event.setEventType(matchedEvent.getEventType());
        event.setChannelId(matchedEvent.getChannelId());
        event.setTimestamp(matchedEvent.getTimestamp());
        event.setWorkSessionId(matchedEvent.getWorkSessionId());
        event.setDescription(matchedEvent.getDescription());
        return event;
    }
}
