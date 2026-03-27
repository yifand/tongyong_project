package com.pdi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 验证码响应VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 验证码key
     */
    private String captchaKey;

    /**
     * 验证码图片（Base64）
     */
    private String captchaImage;

    /**
     * 过期时间（秒）
     */
    private Integer expireSeconds;
}
