package com.pdi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 重启结果VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebootResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 命令ID
     */
    private String commandId;

    /**
     * 状态
     */
    private String status;
}
