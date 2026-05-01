package com.vdc.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("alarm")
public class Alarm implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String alarmType;

    private Long siteId;

    private Long channelId;

    private Long workSessionId;

    private LocalDateTime alarmTime;

    private String processStatus;

    private Long processedBy;

    private LocalDateTime processedAt;

    private String targetImage;

    private String sceneImage;

    private String watermarkLogo;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
