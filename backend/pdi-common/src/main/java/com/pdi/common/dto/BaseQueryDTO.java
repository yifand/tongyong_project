package com.pdi.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础查询DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class BaseQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码（从1开始）
     */
    private Long page = 1L;

    /**
     * 每页大小
     */
    private Long size = 20L;

    /**
     * 排序字段
     */
    private String sort;

}
