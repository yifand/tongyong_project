package com.vdc.platform.ruleengine.core;

import com.vdc.platform.entity.StateStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleEngine {

    private final StateStreamProcessor stateStreamProcessor;

    public void process(StateStream stateStream) {
        stateStreamProcessor.process(stateStream);
    }
}
