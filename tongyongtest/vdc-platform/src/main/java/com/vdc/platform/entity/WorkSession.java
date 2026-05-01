package com.vdc.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("work_session")
public class WorkSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long siteId;

    private Long channelId;

    private String vehicleInfo;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer actualDuration;

    private Integer standardDuration;

    private BigDecimal deviationPct;

    private String result;

    private String snapshotHead;

    private String snapshotTail;

    private String snapshotMid;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
