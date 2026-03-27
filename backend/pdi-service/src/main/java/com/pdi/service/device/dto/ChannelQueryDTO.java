package com.pdi.service.device.dto;

import com.pdi.common.dto.BaseQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通道查询DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChannelQueryDTO extends BaseQueryDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 状态: 0-禁用, 1-启用, 2-故障
     */
    private Integer status;

    /**
     * 关键词搜索(编码/名称)
     */
    private String keyword;

}
