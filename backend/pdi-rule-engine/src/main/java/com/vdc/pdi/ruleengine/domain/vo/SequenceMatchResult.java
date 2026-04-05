package com.vdc.pdi.ruleengine.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 序列匹配结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SequenceMatchResult {

    /**
     * 是否匹配成功
     */
    private boolean matched;

    /**
     * 匹配时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 关联原始状态流记录ID
     */
    private Long stateStreamId;

    /**
     * 匹配类型
     */
    private MatchType matchType;

    /**
     * 匹配类型枚举
     */
    public enum MatchType {
        /**
         * 进入序列匹配
         */
        ENTRY,

        /**
         * 离开序列匹配
         */
        EXIT
    }

    /**
     * 创建匹配成功结果
     */
    public static SequenceMatchResult matched(LocalDateTime timestamp, Long stateStreamId, MatchType matchType) {
        return SequenceMatchResult.builder()
                .matched(true)
                .timestamp(timestamp)
                .stateStreamId(stateStreamId)
                .matchType(matchType)
                .build();
    }

    /**
     * 创建匹配失败结果
     */
    public static SequenceMatchResult notMatched() {
        return SequenceMatchResult.builder()
                .matched(false)
                .build();
    }
}
