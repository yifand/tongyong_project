package com.vdc.pdi.ruleengine.domain.vo;

import com.vdc.pdi.common.enums.StateCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 状态机信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateMachineInfoResponse {

    /**
     * 状态机键 (siteId:channelId)
     */
    private String key;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 当前状态
     */
    private StateCodeEnum currentState;

    /**
     * 状态名称
     */
    private String stateName;
}
