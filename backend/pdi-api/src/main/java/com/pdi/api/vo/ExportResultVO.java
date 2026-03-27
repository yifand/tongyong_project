package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 导出结果VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 过期时间（秒）
     */
    private Integer expireSeconds;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 记录数
     */
    private Long recordCount;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;
}
