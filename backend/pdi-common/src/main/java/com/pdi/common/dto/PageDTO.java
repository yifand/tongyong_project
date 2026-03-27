package com.pdi.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果DTO
 *
 * @param <T> 数据类型
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class PageDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 当前页码
     */
    private Long page;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    public PageDTO() {
    }

    public PageDTO(Long total, List<T> records, Long page, Long size) {
        this.total = total;
        this.records = records;
        this.page = page;
        this.size = size;
        this.pages = (total + size - 1) / size;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageDTO<T> of(Long total, List<T> records, Long page, Long size) {
        return new PageDTO<>(total, records, page, size);
    }

}
