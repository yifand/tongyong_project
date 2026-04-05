package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.ruleengine.domain.vo.SequenceMatchResult;
import com.vdc.pdi.ruleengine.domain.vo.StateSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StateSequenceMatcher单元测试
 */
class StateSequenceMatcherTest {

    private StateSequenceMatcher matcher;
    private List<StateSnapshot> stateHistory;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        matcher = new StateSequenceMatcher();
        stateHistory = new ArrayList<>();
        baseTime = LocalDateTime.now();
    }

    @Test
    @DisplayName("进入序列匹配测试 - S7→S8→S3")
    void matchEntrySequence_ValidSequence_ShouldMatch() {
        // 构建进入序列
        addStateSnapshot(StateCodeEnum.S7, 1L);
        addStateSnapshot(StateCodeEnum.S8, 2L);
        addStateSnapshot(StateCodeEnum.S3, 3L);

        SequenceMatchResult result = matcher.matchEntrySequence(stateHistory);

        assertTrue(result.isMatched());
        assertEquals(SequenceMatchResult.MatchType.ENTRY, result.getMatchType());
        assertEquals(3L, result.getStateStreamId());
    }

    @Test
    @DisplayName("进入序列匹配测试 - 不匹配")
    void matchEntrySequence_InvalidSequence_ShouldNotMatch() {
        // 构建不匹配序列 S1→S5→S7
        addStateSnapshot(StateCodeEnum.S1, 1L);
        addStateSnapshot(StateCodeEnum.S5, 2L);
        addStateSnapshot(StateCodeEnum.S7, 3L);

        SequenceMatchResult result = matcher.matchEntrySequence(stateHistory);

        assertFalse(result.isMatched());
    }

    @Test
    @DisplayName("进入序列匹配测试 - 历史不足3条")
    void matchEntrySequence_InsufficientHistory_ShouldNotMatch() {
        addStateSnapshot(StateCodeEnum.S7, 1L);
        addStateSnapshot(StateCodeEnum.S8, 2L);

        SequenceMatchResult result = matcher.matchEntrySequence(stateHistory);

        assertFalse(result.isMatched());
    }

    @Test
    @DisplayName("离开序列匹配测试 - S3→S8→S7→S5")
    void matchExitSequence_ValidSequenceS5_ShouldMatch() {
        // 构建离开序列
        addStateSnapshot(StateCodeEnum.S3, 1L);
        addStateSnapshot(StateCodeEnum.S8, 2L);
        addStateSnapshot(StateCodeEnum.S7, 3L);
        addStateSnapshot(StateCodeEnum.S5, 4L);

        SequenceMatchResult result = matcher.matchExitSequence(stateHistory);

        assertTrue(result.isMatched());
        assertEquals(SequenceMatchResult.MatchType.EXIT, result.getMatchType());
        assertEquals(4L, result.getStateStreamId());
    }

    @Test
    @DisplayName("离开序列匹配测试 - S3→S8→S7→S1")
    void matchExitSequence_ValidSequenceS1_ShouldMatch() {
        // 构建离开序列
        addStateSnapshot(StateCodeEnum.S3, 1L);
        addStateSnapshot(StateCodeEnum.S8, 2L);
        addStateSnapshot(StateCodeEnum.S7, 3L);
        addStateSnapshot(StateCodeEnum.S1, 4L);

        SequenceMatchResult result = matcher.matchExitSequence(stateHistory);

        assertTrue(result.isMatched());
        assertEquals(SequenceMatchResult.MatchType.EXIT, result.getMatchType());
    }

    @Test
    @DisplayName("离开序列匹配测试 - 不匹配")
    void matchExitSequence_InvalidSequence_ShouldNotMatch() {
        // 构建不匹配序列 S3→S8→S7→S3
        addStateSnapshot(StateCodeEnum.S3, 1L);
        addStateSnapshot(StateCodeEnum.S8, 2L);
        addStateSnapshot(StateCodeEnum.S7, 3L);
        addStateSnapshot(StateCodeEnum.S3, 4L);

        SequenceMatchResult result = matcher.matchExitSequence(stateHistory);

        assertFalse(result.isMatched());
    }

    @Test
    @DisplayName("离开序列匹配测试 - 历史不足4条")
    void matchExitSequence_InsufficientHistory_ShouldNotMatch() {
        addStateSnapshot(StateCodeEnum.S3, 1L);
        addStateSnapshot(StateCodeEnum.S8, 2L);
        addStateSnapshot(StateCodeEnum.S7, 3L);

        SequenceMatchResult result = matcher.matchExitSequence(stateHistory);

        assertFalse(result.isMatched());
    }

    @Test
    @DisplayName("滑动窗口匹配测试 - 进入序列")
    void slidingWindowMatch_EntrySequence() {
        // 构建包含多个进入序列的历史
        // S1→S5→S7→S8→S3→S5→S7→S8→S3
        addStateSnapshot(StateCodeEnum.S1, 1L);
        addStateSnapshot(StateCodeEnum.S5, 2L);
        addStateSnapshot(StateCodeEnum.S7, 3L);
        addStateSnapshot(StateCodeEnum.S8, 4L);
        addStateSnapshot(StateCodeEnum.S3, 5L);
        addStateSnapshot(StateCodeEnum.S5, 6L);
        addStateSnapshot(StateCodeEnum.S7, 7L);
        addStateSnapshot(StateCodeEnum.S8, 8L);
        addStateSnapshot(StateCodeEnum.S3, 9L);

        List<SequenceMatchResult> matches = matcher.slidingWindowMatch(
                stateHistory,
                List.of(StateCodeEnum.S7, StateCodeEnum.S8, StateCodeEnum.S3)
        );

        assertEquals(2, matches.size());
        assertEquals(5L, matches.get(0).getStateStreamId());
        assertEquals(9L, matches.get(1).getStateStreamId());
    }

    @Test
    @DisplayName("滑动窗口匹配测试 - 无匹配")
    void slidingWindowMatch_NoMatch() {
        addStateSnapshot(StateCodeEnum.S1, 1L);
        addStateSnapshot(StateCodeEnum.S5, 2L);
        addStateSnapshot(StateCodeEnum.S7, 3L);

        List<SequenceMatchResult> matches = matcher.slidingWindowMatch(
                stateHistory,
                List.of(StateCodeEnum.S7, StateCodeEnum.S8, StateCodeEnum.S3)
        );

        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("查找所有进入匹配测试")
    void findAllEntryMatches_MultipleMatches() {
        // S7→S8→S3 (匹配1)
        // S3→S8→S7→S5 (离开，不匹配)
        // S5→S7→S8→S3 (匹配2)
        addStateSnapshot(StateCodeEnum.S7, 1L);
        addStateSnapshot(StateCodeEnum.S8, 2L);
        addStateSnapshot(StateCodeEnum.S3, 3L);
        addStateSnapshot(StateCodeEnum.S8, 4L);
        addStateSnapshot(StateCodeEnum.S7, 5L);
        addStateSnapshot(StateCodeEnum.S5, 6L);
        addStateSnapshot(StateCodeEnum.S7, 7L);
        addStateSnapshot(StateCodeEnum.S8, 8L);
        addStateSnapshot(StateCodeEnum.S3, 9L);

        List<SequenceMatchResult> matches = matcher.findAllEntryMatches(stateHistory);

        assertEquals(2, matches.size());
        assertEquals(SequenceMatchResult.MatchType.ENTRY, matches.get(0).getMatchType());
        assertEquals(SequenceMatchResult.MatchType.ENTRY, matches.get(1).getMatchType());
    }

    @Test
    @DisplayName("isLastStatesEntrySequence测试 - 最后状态匹配进入序列")
    void isLastStatesEntrySequence_LastStatesMatch() {
        addStateSnapshot(StateCodeEnum.S1, 1L);
        addStateSnapshot(StateCodeEnum.S7, 2L);
        addStateSnapshot(StateCodeEnum.S8, 3L);
        addStateSnapshot(StateCodeEnum.S3, 4L);

        assertTrue(matcher.isLastStatesEntrySequence(stateHistory));
        assertFalse(matcher.isLastStatesExitSequence(stateHistory));
    }

    @Test
    @DisplayName("isLastStatesExitSequence测试 - 最后状态匹配离开序列")
    void isLastStatesExitSequence_LastStatesMatch() {
        addStateSnapshot(StateCodeEnum.S7, 1L);
        addStateSnapshot(StateCodeEnum.S8, 2L);
        addStateSnapshot(StateCodeEnum.S3, 3L);
        addStateSnapshot(StateCodeEnum.S8, 4L);
        addStateSnapshot(StateCodeEnum.S7, 5L);
        addStateSnapshot(StateCodeEnum.S5, 6L);

        assertTrue(matcher.isLastStatesExitSequence(stateHistory));
        assertFalse(matcher.isLastStatesEntrySequence(stateHistory));
    }

    @Test
    @DisplayName("复杂场景测试 - 多序列混合")
    void complexScenario_MultipleSequences() {
        // 模拟真实场景
        // S1→S5→S7→S8→S3 (进入)
        // S3→S8→S7→S5 (离开)
        // S5→S7→S8→S3 (再次进入)
        addStateSnapshot(StateCodeEnum.S1, 1L);
        addStateSnapshot(StateCodeEnum.S5, 2L);
        addStateSnapshot(StateCodeEnum.S7, 3L);
        addStateSnapshot(StateCodeEnum.S8, 4L);
        addStateSnapshot(StateCodeEnum.S3, 5L);

        assertTrue(matcher.matchEntrySequence(stateHistory).isMatched());
        assertFalse(matcher.matchExitSequence(stateHistory).isMatched());

        addStateSnapshot(StateCodeEnum.S8, 6L);
        addStateSnapshot(StateCodeEnum.S7, 7L);
        addStateSnapshot(StateCodeEnum.S5, 8L);

        assertTrue(matcher.matchExitSequence(stateHistory).isMatched());

        addStateSnapshot(StateCodeEnum.S7, 9L);
        addStateSnapshot(StateCodeEnum.S8, 10L);
        addStateSnapshot(StateCodeEnum.S3, 11L);

        assertTrue(matcher.matchEntrySequence(stateHistory).isMatched());
    }

    @Test
    @DisplayName("空历史测试")
    void emptyHistory_ShouldNotMatch() {
        assertFalse(matcher.matchEntrySequence(stateHistory).isMatched());
        assertFalse(matcher.matchExitSequence(stateHistory).isMatched());
    }

    private void addStateSnapshot(StateCodeEnum state, Long streamId) {
        stateHistory.add(StateSnapshot.builder()
                .state(state)
                .timestamp(baseTime.plusSeconds(stateHistory.size()))
                .stateStreamId(streamId)
                .channelId(1L)
                .siteId(1L)
                .build());
    }
}
