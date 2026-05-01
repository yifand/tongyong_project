package com.vdc.platform.ruleengine.model;

import com.vdc.platform.entity.StateStream;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;

@Data
public class ChannelStateContext {

    public enum State {
        IDLE,
        ENTERING,
        WORKING,
        EXITING
    }

    private static final int MAX_WINDOW_SIZE = 64;

    private String channelId;
    private Deque<StateStream> window = new ArrayDeque<>(MAX_WINDOW_SIZE);
    private Long currentWorkSessionId;
    private State state = State.IDLE;
    private LocalDateTime lastPersonPresentTimestamp;
    private LocalDateTime enterTimestamp;
    private boolean personAbsentViolationTriggered = false;

    public void addState(StateStream stateStream) {
        if (window.size() >= MAX_WINDOW_SIZE) {
            window.pollFirst();
        }
        window.offerLast(stateStream);
    }

    public void clearWindow() {
        window.clear();
    }

    public void updatePersonPresentTimestamp(LocalDateTime timestamp) {
        this.lastPersonPresentTimestamp = timestamp;
    }

    public void resetViolationFlag() {
        this.personAbsentViolationTriggered = false;
    }
}
