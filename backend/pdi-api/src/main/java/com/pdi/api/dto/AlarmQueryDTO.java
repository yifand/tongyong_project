package com.pdi.api.dto;

import com.pdi.common.dto.PageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预警查询DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AlarmQueryDTO extends PageDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 报警类型（1-PDI超时，2-违规吸烟，3-门异常开启，4-人员异常）
     */
    private Integer alarmType;

    /**
     * 报警级别（1-低，2-中，3-高，4-紧急）
     */
    private Integer alarmLevel;

    /**
     * 处理状态（0-未处理，1-已确认，2-已处理，3-误报）
     */
    private Integer handleStatus;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 关键词（站点/通道名称）
     */
    private String keyword;
}
