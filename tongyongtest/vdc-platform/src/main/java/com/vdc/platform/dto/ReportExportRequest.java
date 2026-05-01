package com.vdc.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportExportRequest {

    private List<Long> siteIds;
    private List<Long> channelIds;
    private String result;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 导出格式: xlsx, pdf, zip (默认 xlsx) */
    private String format;
    /** 是否包含截图 (zip 格式时有效) */
    private Boolean includeImages;
}
