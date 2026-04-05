package com.vdc.pdi.device.dto.request;

import lombok.Data;

/**
 * 通道查询请求
 */
@Data
public class ChannelQueryRequest {

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 算法类型
     */
    private String algorithmType;

    /**
     * 关键字搜索
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
