package com.pdi.service.archive.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 档案完成DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class ArchiveCompleteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 进入状态序列
     */
    private String enterStateSeq;

    /**
     * 离开状态序列
     */
    private String exitStateSeq;

    /**
     * 结束图片URL
     */
    private String endImageUrl;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 是否有抽烟行为
     */
    private Boolean hasSmoking;

    /**
     * 备注
     */
    private String remark;

}
