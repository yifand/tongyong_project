package com.pdi.service.alarm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 预警处理DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class AlarmHandleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 处理结果
     */
    @NotBlank(message = "处理结果不能为空")
    private String handleResult;

    /**
     * 处理备注
     */
    private String handleRemark;

}
