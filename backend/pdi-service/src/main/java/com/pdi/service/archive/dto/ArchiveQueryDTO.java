package com.pdi.service.archive.dto;

import com.pdi.common.dto.BaseQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 档案查询DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArchiveQueryDTO extends BaseQueryDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 作业结果: 1-合格, 2-超时, 3-异常
     */
    private Integer taskResult;

    /**
     * 关键词搜索(作业编号/通道名称)
     */
    private String keyword;

}
