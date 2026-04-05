package com.vdc.pdi.device.dto.request;

import lombok.Data;

/**
 * 盒子查询请求
 */
@Data
public class BoxQueryRequest {

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 关键字搜索（名称/IP）
     */
    private String keyword;

    /**
     * 状态：0-离线，1-在线
     */
    private Integer status;

    /**
     * 页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页大小，默认20
     */
    private Integer size = 20;
}
