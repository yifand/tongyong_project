package com.vdc.pdi.ruleengine.domain.vo;

import com.vdc.pdi.common.enums.StateCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 状态快照
 * 记录状态转换的历史信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateSnapshot {

    /**
     * 状态编码
     */
    private StateCodeEnum state;

    /**
     * 状态发生时间
     */
    private LocalDateTime timestamp;

    /**
     * 关联原始状态流记录ID
     */
    private Long stateStreamId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 站点ID
     */
    private Long siteId;
}
