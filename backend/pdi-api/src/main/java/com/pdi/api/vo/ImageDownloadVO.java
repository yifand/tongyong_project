package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图片下载信息VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDownloadVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 下载URL
     */
    private String url;

    /**
     * 过期时间（秒）
     */
    private Integer expireSeconds;
}
