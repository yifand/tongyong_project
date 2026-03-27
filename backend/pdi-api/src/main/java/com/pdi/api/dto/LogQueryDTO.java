package com.pdi.api.dto;

import com.pdi.common.dto.PageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 日志查询DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LogQueryDTO extends PageDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 用户名
     */
    private String username;

    /**
     * 状态（0-失败，1-成功）
     */
    private Integer status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
