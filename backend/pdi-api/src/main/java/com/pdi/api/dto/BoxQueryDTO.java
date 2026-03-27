package com.pdi.api.dto;

import com.pdi.common.dto.PageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 盒子查询DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BoxQueryDTO extends PageDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 状态（0-离线，1-在线，2-故障）
     */
    private Integer status;

    /**
     * 关键词（编码/名称）
     */
    private String keyword;
}
