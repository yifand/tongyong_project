package com.vdc.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vdc.platform.entity.Alarm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmPageQuery extends Page<Alarm> {

    private String alarmType;
    private Long siteId;
    private Long channelId;
    private String processStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
