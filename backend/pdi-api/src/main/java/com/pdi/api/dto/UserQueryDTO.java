package com.pdi.api.dto;

import com.pdi.common.dto.PageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户查询DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryDTO extends PageDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 关键词（用户名/真实姓名）
     */
    private String keyword;
}
