package com.vdc.pdi.device.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 盒子状态值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxStatus {

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 是否在线
     */
    private Boolean online;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatAt;

    /**
     * 心跳超时秒数
     */
    private Integer timeoutSeconds;
}
