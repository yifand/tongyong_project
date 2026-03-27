package com.pdi.service.archive.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案详情VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArchiveDetailVO extends ArchiveVO {

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
     * 视频URL
     */
    private String videoUrl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关联报警列表
     */
    private List<ArchiveAlarmVO> alarms;

    /**
     * 档案报警VO
     */
    @Data
    public static class ArchiveAlarmVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private Integer alarmType;
        private String alarmTypeName;
        private LocalDateTime alarmTime;
    }

}
