package com.pdi.service.device.dto;

import com.pdi.common.dto.BaseQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 盒子查询DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BoxQueryDTO extends BaseQueryDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 点位ID
     */
    private Long siteId;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 状态: 0-离线, 1-在线, 2-故障
     */
    private Integer status;

    /**
     * 关键词搜索(编码/名称)
     */
    private String keyword;

}
