package com.pdi.receiver.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

/**
 * PDI状态机监听器
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component
public class PDIStateMachineListener extends StateMachineListenerAdapter<PDIState, PDIEvent> {

    @Override
    public void stateChanged(State<PDIState, PDIEvent> from, State<PDIState, PDIEvent> to) {
        if (from != null && to != null) {
            log.info("PDI状态变更: {} -> {}", from.getId(), to.getId());
        } else if (to != null) {
            log.info("PDI状态初始化: {}", to.getId());
        }
    }

    @Override
    public void transition(Transition<PDIState, PDIEvent> transition) {
        if (transition.getSource() != null && transition.getTarget() != null) {
            log.debug("PDI状态转换: {} -> {} via {}",
                    transition.getSource().getId(),
                    transition.getTarget().getId(),
                    transition.getTrigger() != null ? transition.getTrigger().getEvent() : "AUTO");
        }
    }

    @Override
    public void eventNotAccepted(Message<PDIEvent> event) {
        log.warn("PDI事件未被接受: {}", event.getPayload());
    }

    @Override
    public void stateMachineError(org.springframework.statemachine.StateMachine<PDIState, PDIEvent> stateMachine, Exception exception) {
        log.error("PDI状态机错误: {}", exception.getMessage(), exception);
    }

}
