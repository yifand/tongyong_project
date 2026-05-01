package com.vdc.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vdc.platform.entity.WorkSession;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportPageQuery extends Page<WorkSession> {

    private Long siteId;
    private Long channelId;
    private String result;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
