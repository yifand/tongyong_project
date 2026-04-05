package com.vdc.pdi.ruleengine.domain.entity;

import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.ruleengine.domain.vo.StateSnapshot;
import com.vdc.pdi.ruleengine.domain.vo.StateTransitionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StateMachine单元测试
 */
class StateMachineTest {

    private StateMachine stateMachine;
    private static final Long CHANNEL_ID = 1L;
    private static final Long SITE_ID = 1L;

    @BeforeEach
    void setUp() {
        stateMachine = new StateMachine();
        stateMachine.initialize(CHANNEL_ID, SITE_ID);
    }

    @Test
    @DisplayName("初始化测试 - 状态机应初始化为S1状态")
    void initialization_ShouldStartInS1() {
        assertEquals(StateCodeEnum.S1, stateMachine.getCurrentState());
        assertEquals(CHANNEL_ID, stateMachine.getChannelId());
        assertEquals(SITE_ID, stateMachine.getSiteId());
        assertTrue(stateMachine.isIdle());
        assertFalse(stateMachine.isOccupied());
        assertTrue(stateMachine.getStateHistory().isEmpty());
    }

    @Test
    @DisplayName("有效状态转换测试 - S1到S5")
    void validTransition_S1ToS5() {
        StateTransitionResult result = stateMachine.transition(
                StateCodeEnum.S5, LocalDateTime.now(), 1L);

        assertTrue(result.isValid());
        assertEquals(StateCodeEnum.S1, result.getFromState());
        assertEquals(StateCodeEnum.S5, result.getToState());
        assertEquals(StateCodeEnum.S5, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("有效状态转换测试 - S1到S7")
    void validTransition_S1ToS7() {
        StateTransitionResult result = stateMachine.transition(
                StateCodeEnum.S7, LocalDateTime.now(), 1L);

        assertTrue(result.isValid());
        assertEquals(StateCodeEnum.S7, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("无效状态转换测试 - S1不能直接到S3")
    void invalidTransition_S1ToS3() {
        StateTransitionResult result = stateMachine.transition(
                StateCodeEnum.S3, LocalDateTime.now(), 1L);

        assertFalse(result.isValid());
        assertEquals(StateCodeEnum.S1, stateMachine.getCurrentState());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("相同状态转换测试 - 允许重复状态")
    void sameStateTransition_ShouldBeValid() {
        stateMachine.transition(StateCodeEnum.S5, LocalDateTime.now(), 1L);
        StateTransitionResult result = stateMachine.transition(
                StateCodeEnum.S5, LocalDateTime.now().plusSeconds(1), 2L);

        assertTrue(result.isValid());
        assertEquals(StateCodeEnum.S5, stateMachine.getCurrentState());
    }

    @Test
    @DisplayName("进入序列匹配测试 - S7→S8→S3应匹配进入")
    void entrySequence_S7S8S3_ShouldMatch() {
        LocalDateTime now = LocalDateTime.now();

        // 先转换到S7
        stateMachine.transition(StateCodeEnum.S7, now, 1L);
        // 再到S8
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        // 最后到S3，应触发进入事件
        StateTransitionResult result = stateMachine.transition(
                StateCodeEnum.S3, now.plusSeconds(2), 3L);

        assertTrue(result.isValid());
        assertEquals(StateTransitionResult.RuleTriggerType.ENTRY_DETECTED, result.getTriggerType());
        assertNotNull(result.getEntryTime());
        assertTrue(stateMachine.matchesEntrySequence());
        assertTrue(stateMachine.isOccupied());
    }

    @Test
    @DisplayName("离开序列匹配测试 - S3→S8→S7→S5应匹配离开")
    void exitSequence_S3S8S7S5_ShouldMatch() {
        LocalDateTime now = LocalDateTime.now();

        // 先完成进入序列 S7→S8→S3
        stateMachine.transition(StateCodeEnum.S7, now, 1L);
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        stateMachine.transition(StateCodeEnum.S3, now.plusSeconds(2), 3L);

        // 再完成离开序列 S3→S8→S7→S5
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(3), 4L);
        stateMachine.transition(StateCodeEnum.S7, now.plusSeconds(4), 5L);
        StateTransitionResult result = stateMachine.transition(
                StateCodeEnum.S5, now.plusSeconds(5), 6L);

        assertTrue(result.isValid());
        assertEquals(StateTransitionResult.RuleTriggerType.EXIT_DETECTED, result.getTriggerType());
        assertTrue(stateMachine.matchesExitSequence());
        assertTrue(stateMachine.isIdle());
    }

    @Test
    @DisplayName("离开序列匹配测试 - S3→S8→S7→S1也应匹配离开")
    void exitSequence_S3S8S7S1_ShouldMatch() {
        LocalDateTime now = LocalDateTime.now();

        // 先完成进入序列
        stateMachine.transition(StateCodeEnum.S7, now, 1L);
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        stateMachine.transition(StateCodeEnum.S3, now.plusSeconds(2), 3L);

        // 再完成离开序列，最后到S1
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(3), 4L);
        stateMachine.transition(StateCodeEnum.S7, now.plusSeconds(4), 5L);
        StateTransitionResult result = stateMachine.transition(
                StateCodeEnum.S1, now.plusSeconds(5), 6L);

        assertTrue(result.isValid());
        assertEquals(StateTransitionResult.RuleTriggerType.EXIT_DETECTED, result.getTriggerType());
        assertTrue(stateMachine.matchesExitSequence());
    }

    @Test
    @DisplayName("非离开序列测试 - S3→S8→S7→S3不应匹配离开")
    void nonExitSequence_S3S8S7S3_ShouldNotMatch() {
        LocalDateTime now = LocalDateTime.now();

        // 完成进入序列
        stateMachine.transition(StateCodeEnum.S7, now, 1L);
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        stateMachine.transition(StateCodeEnum.S3, now.plusSeconds(2), 3L);

        // S3→S8→S7→S3 不是离开序列
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(3), 4L);
        stateMachine.transition(StateCodeEnum.S7, now.plusSeconds(4), 5L);
        StateTransitionResult result = stateMachine.transition(
                StateCodeEnum.S3, now.plusSeconds(5), 6L);

        assertTrue(result.isValid());
        assertEquals(StateTransitionResult.RuleTriggerType.NONE, result.getTriggerType());
        assertFalse(stateMachine.matchesExitSequence());
    }

    @Test
    @DisplayName("历史记录限制测试 - 超过100条应移除最旧的")
    void historyLimit_ShouldRemoveOldest() {
        LocalDateTime baseTime = LocalDateTime.now();

        // 添加105条历史记录
        for (int i = 0; i < 105; i++) {
            stateMachine.transition(StateCodeEnum.S5, baseTime.plusSeconds(i), (long) i);
            stateMachine.transition(StateCodeEnum.S1, baseTime.plusSeconds(i + 1), (long) i + 1000);
        }

        List<StateSnapshot> history = stateMachine.getStateHistory();
        assertTrue(history.size() <= 100, "历史记录不应超过100条");
    }

    @Test
    @DisplayName("获取最近状态测试 - getRecentStates")
    void getRecentStates_ShouldReturnLastN() {
        LocalDateTime now = LocalDateTime.now();

        // 创建进入序列
        stateMachine.transition(StateCodeEnum.S7, now, 1L);
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        stateMachine.transition(StateCodeEnum.S3, now.plusSeconds(2), 3L);

        List<StateSnapshot> recent = stateMachine.getRecentStates(3);
        assertEquals(3, recent.size());
        assertEquals(StateCodeEnum.S7, recent.get(0).getState());
        assertEquals(StateCodeEnum.S8, recent.get(1).getState());
        assertEquals(StateCodeEnum.S3, recent.get(2).getState());
    }

    @Test
    @DisplayName("获取进入时间测试")
    void getEntryTime_ShouldReturnS3Timestamp() {
        LocalDateTime now = LocalDateTime.now();

        stateMachine.transition(StateCodeEnum.S7, now, 1L);
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        LocalDateTime entryTime = now.plusSeconds(2);
        stateMachine.transition(StateCodeEnum.S3, entryTime, 3L);

        assertEquals(entryTime, stateMachine.getEntryTime());
    }

    @Test
    @DisplayName("获取离开时间测试")
    void getExitTime_ShouldReturnS5Timestamp() {
        LocalDateTime now = LocalDateTime.now();

        // 进入
        stateMachine.transition(StateCodeEnum.S7, now, 1L);
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        stateMachine.transition(StateCodeEnum.S3, now.plusSeconds(2), 3L);

        // 离开
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(3), 4L);
        stateMachine.transition(StateCodeEnum.S7, now.plusSeconds(4), 5L);
        LocalDateTime exitTime = now.plusSeconds(5);
        stateMachine.transition(StateCodeEnum.S5, exitTime, 6L);

        assertEquals(exitTime, stateMachine.getExitTime());
    }

    @Test
    @DisplayName("重置状态机测试")
    void reset_ShouldClearAllState() {
        LocalDateTime now = LocalDateTime.now();

        stateMachine.transition(StateCodeEnum.S7, now, 1L);
        stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        stateMachine.transition(StateCodeEnum.S3, now.plusSeconds(2), 3L);

        stateMachine.reset();

        assertEquals(StateCodeEnum.S1, stateMachine.getCurrentState());
        assertTrue(stateMachine.getStateHistory().isEmpty());
        assertNull(stateMachine.getEntryTime());
        assertTrue(stateMachine.isIdle());
    }

    @Test
    @DisplayName("完整状态机周期测试 - 进入和离开")
    void fullCycle_EntryAndExit() {
        LocalDateTime now = LocalDateTime.now();

        // 进入: S1→S7→S8→S3
        StateTransitionResult r1 = stateMachine.transition(StateCodeEnum.S7, now, 1L);
        assertEquals(StateTransitionResult.RuleTriggerType.NONE, r1.getTriggerType());

        StateTransitionResult r2 = stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(1), 2L);
        assertEquals(StateTransitionResult.RuleTriggerType.NONE, r2.getTriggerType());

        StateTransitionResult r3 = stateMachine.transition(StateCodeEnum.S3, now.plusSeconds(2), 3L);
        assertEquals(StateTransitionResult.RuleTriggerType.ENTRY_DETECTED, r3.getTriggerType());

        // 离开: S3→S8→S7→S5
        StateTransitionResult r4 = stateMachine.transition(StateCodeEnum.S8, now.plusSeconds(600), 4L);
        assertEquals(StateTransitionResult.RuleTriggerType.NONE, r4.getTriggerType());

        StateTransitionResult r5 = stateMachine.transition(StateCodeEnum.S7, now.plusSeconds(601), 5L);
        assertEquals(StateTransitionResult.RuleTriggerType.NONE, r5.getTriggerType());

        StateTransitionResult r6 = stateMachine.transition(StateCodeEnum.S5, now.plusSeconds(602), 6L);
        assertEquals(StateTransitionResult.RuleTriggerType.EXIT_DETECTED, r6.getTriggerType());
        assertNotNull(r6.getEntryTime());
    }
}
