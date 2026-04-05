package com.vdc.pdi.alarm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 报警处理请求
 */
@Data
@Schema(description = "报警处理请求")
public class AlarmProcessRequest {

    @Schema(description = "处理备注", example = "已核实，确认为违规行为")
    @Size(max = 500)
    private String remark;
}
