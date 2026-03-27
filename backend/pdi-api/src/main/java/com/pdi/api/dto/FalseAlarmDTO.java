package com.pdi.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 误报标记DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FalseAlarmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 误报原因
     */
    @NotBlank(message = "误报原因不能为空")
    private String reason;
}
