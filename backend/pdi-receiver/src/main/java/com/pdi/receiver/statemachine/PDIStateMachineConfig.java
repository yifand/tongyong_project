package com.pdi.receiver.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * PDI状态机配置
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Configuration
@EnableStateMachineFactory
public class PDIStateMachineConfig extends StateMachineConfigurerAdapter<PDIState, PDIEvent> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<PDIState, PDIEvent> config) throws Exception {
        config
            .withConfiguration()
            .autoStartup(false);
    }

    @Override
    public void configure(StateMachineStateConfigurer<PDIState, PDIEvent> states) throws Exception {
        states
            .withStates()
            .initial(PDIState.S1)
            .states(EnumSet.allOf(PDIState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PDIState, PDIEvent> transitions) throws Exception {
        transitions
            // S1 -> S3: 门打开
            .withExternal()
                .source(PDIState.S1)
                .target(PDIState.S3)
                .event(PDIEvent.DOOR_OPEN)
                .and()

            // S3 -> S7: 检测到有人进入
            .withExternal()
                .source(PDIState.S3)
                .target(PDIState.S7)
                .event(PDIEvent.PERSON_DETECTED)
                .and()

            // S7 -> S8: 检测到进入动作
            .withExternal()
                .source(PDIState.S7)
                .target(PDIState.S8)
                .event(PDIEvent.ENTERING_DETECTED)
                .and()

            // S8 -> S5: 门关闭（进入完成，开始PDI）
            .withExternal()
                .source(PDIState.S8)
                .target(PDIState.S5)
                .event(PDIEvent.ENTER_COMPLETE)
                .and()

            // S5 -> S8: 门打开，准备离开
            .withExternal()
                .source(PDIState.S5)
                .target(PDIState.S8)
                .event(PDIEvent.DOOR_OPEN)
                .and()

            // S8 -> S1: 门关闭（离开完成，结束PDI）
            .withExternal()
                .source(PDIState.S8)
                .target(PDIState.S1)
                .event(PDIEvent.EXIT_COMPLETE)
                .and()

            // S7 -> S3: 人离开
            .withExternal()
                .source(PDIState.S7)
                .target(PDIState.S3)
                .event(PDIEvent.PERSON_LEFT)
                .and()

            // S3 -> S1: 门关闭
            .withExternal()
                .source(PDIState.S3)
                .target(PDIState.S1)
                .event(PDIEvent.DOOR_CLOSE)
                .and()

            // S5 -> S1: 异常重置
            .withExternal()
                .source(PDIState.S5)
                .target(PDIState.S1)
                .event(PDIEvent.ERROR)
                .and()

            // S8 -> S1: 异常重置
            .withExternal()
                .source(PDIState.S8)
                .target(PDIState.S1)
                .event(PDIEvent.ERROR);
    }

}
