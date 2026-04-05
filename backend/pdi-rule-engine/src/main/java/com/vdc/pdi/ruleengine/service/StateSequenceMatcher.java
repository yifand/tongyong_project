package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.ruleengine.domain.vo.SequenceMatchResult;
import com.vdc.pdi.ruleengine.domain.vo.StateSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 状态序列匹配器
 * 实现滑动窗口匹配算法，识别进入/离开序列
 */
@Component
@Slf4j
public class StateSequenceMatcher {

    /**
     * 进入序列: S7 → S8 → S3
     */
    private static final List<StateCodeEnum> ENTRY_SEQUENCE = List.of(
            StateCodeEnum.S7, StateCodeEnum.S8, StateCodeEnum.S3
    );

    /**
     * 离开序列前缀: S3 → S8 → S7
     */
    private static final List<StateCodeEnum> EXIT_SEQUENCE_PREFIX = List.of(
            StateCodeEnum.S3, StateCodeEnum.S8, StateCodeEnum.S7
    );

    /**
     * 离开序列结束状态
     */
    private static final Set<StateCodeEnum> EXIT_END_STATES = Set.of(
            StateCodeEnum.S5, StateCodeEnum.S1
    );

    /**
     * 匹配进入序列
     *
     * @param stateHistory 历史状态序列
     * @return 匹配结果
     */
    public SequenceMatchResult matchEntrySequence(List<StateSnapshot> stateHistory) {
        if (stateHistory.size() < ENTRY_SEQUENCE.size()) {
            return SequenceMatchResult.notMatched();
        }

        // 取最近3个状态
        List<StateSnapshot> recent = getLastN(stateHistory, 3);

        for (int i = 0; i < ENTRY_SEQUENCE.size(); i++) {
            if (recent.get(i).getState() != ENTRY_SEQUENCE.get(i)) {
                return SequenceMatchResult.notMatched();
            }
        }

        // 匹配成功，返回进入时间 (S3的时间戳)
        StateSnapshot lastSnapshot = recent.get(2);
        log.debug("匹配进入序列成功: stateStreamId={}, entryTime={}",
                lastSnapshot.getStateStreamId(), lastSnapshot.getTimestamp());

        return SequenceMatchResult.matched(
                lastSnapshot.getTimestamp(),
                lastSnapshot.getStateStreamId(),
                SequenceMatchResult.MatchType.ENTRY
        );
    }

    /**
     * 匹配离开序列
     *
     * @param stateHistory 历史状态序列
     * @return 匹配结果
     */
    public SequenceMatchResult matchExitSequence(List<StateSnapshot> stateHistory) {
        if (stateHistory.size() < 4) {
            return SequenceMatchResult.notMatched();
        }

        // 取最近4个状态
        List<StateSnapshot> recent = getLastN(stateHistory, 4);

        // 检查前3个状态是否匹配 S3 → S8 → S7
        for (int i = 0; i < EXIT_SEQUENCE_PREFIX.size(); i++) {
            if (recent.get(i).getState() != EXIT_SEQUENCE_PREFIX.get(i)) {
                return SequenceMatchResult.notMatched();
            }
        }

        // 检查最后一个状态是否为 S5 或 S1
        StateCodeEnum lastState = recent.get(3).getState();
        if (!EXIT_END_STATES.contains(lastState)) {
            return SequenceMatchResult.notMatched();
        }

        // 匹配成功，返回离开时间
        StateSnapshot lastSnapshot = recent.get(3);
        log.debug("匹配离开序列成功: stateStreamId={}, exitTime={}",
                lastSnapshot.getStateStreamId(), lastSnapshot.getTimestamp());

        return SequenceMatchResult.matched(
                lastSnapshot.getTimestamp(),
                lastSnapshot.getStateStreamId(),
                SequenceMatchResult.MatchType.EXIT
        );
    }

    /**
     * 滑动窗口匹配（用于复杂场景）
     *
     * @param stateHistory 历史状态序列
     * @param pattern      待匹配的模式
     * @return 所有匹配位置
     */
    public List<SequenceMatchResult> slidingWindowMatch(
            List<StateSnapshot> stateHistory,
            List<StateCodeEnum> pattern) {

        List<SequenceMatchResult> matches = new ArrayList<>();

        if (stateHistory.size() < pattern.size()) {
            return matches;
        }

        // 滑动窗口
        for (int i = 0; i <= stateHistory.size() - pattern.size(); i++) {
            boolean matched = true;

            for (int j = 0; j < pattern.size(); j++) {
                if (stateHistory.get(i + j).getState() != pattern.get(j)) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                StateSnapshot lastSnapshot = stateHistory.get(i + pattern.size() - 1);
                matches.add(SequenceMatchResult.matched(
                        lastSnapshot.getTimestamp(),
                        lastSnapshot.getStateStreamId(),
                        SequenceMatchResult.MatchType.ENTRY
                ));
            }
        }

        return matches;
    }

    /**
     * 查找所有进入序列匹配
     */
    public List<SequenceMatchResult> findAllEntryMatches(List<StateSnapshot> stateHistory) {
        return slidingWindowMatch(stateHistory, ENTRY_SEQUENCE);
    }

    /**
     * 检查状态列表的最后N个是否匹配进入序列
     */
    public boolean isLastStatesEntrySequence(List<StateSnapshot> stateHistory) {
        return matchEntrySequence(stateHistory).isMatched();
    }

    /**
     * 检查状态列表的最后N个是否匹配离开序列
     */
    public boolean isLastStatesExitSequence(List<StateSnapshot> stateHistory) {
        return matchExitSequence(stateHistory).isMatched();
    }

    private List<StateSnapshot> getLastN(List<StateSnapshot> list, int n) {
        int size = list.size();
        if (size <= n) {
            return new ArrayList<>(list);
        }
        return new ArrayList<>(list.subList(size - n, size));
    }
}
