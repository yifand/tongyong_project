package com.vdc.pdi.ruleengine.domain.vo;

import com.vdc.pdi.common.enums.StateCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 状态信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateInfoResponse {

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 当前状态
     */
    private StateCodeEnum currentState;

    /**
     * 状态名称
     */
    private String stateName;

    /**
     * 最近状态历史
     */
    private List<StateSnapshot> recentHistory;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;
}
