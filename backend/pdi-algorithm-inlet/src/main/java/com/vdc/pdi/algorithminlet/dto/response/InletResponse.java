package com.vdc.pdi.algorithminlet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * 算法数据入口统一响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "算法数据入口统一响应")
public class InletResponse {

    @Schema(description = "状态码: 200=成功, 400=参数错误, 401=认证失败, 404=资源不存在, 500=服务器错误", example = "200")
    private Integer code;

    @Schema(description = "消息", example = "success")
    private String message;

    @Schema(description = "数据")
    private Object data;

    @Schema(description = "时间戳", example = "1711699200000")
    private Long timestamp;

    /**
     * 成功响应
     */
    public static InletResponse success() {
        return InletResponse.builder()
                .code(200)
                .message("success")
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static InletResponse success(Object data) {
        return InletResponse.builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * 失败响应
     */
    public static InletResponse fail(String errorCode, String message) {
        return InletResponse.builder()
                .code(400)
                .message(message)
                .data(Collections.singletonMap("errorCode", errorCode))
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * 失败响应（带HTTP状态码）
     */
    public static InletResponse fail(int httpCode, String errorCode, String message) {
        return InletResponse.builder()
                .code(httpCode)
                .message(message)
                .data(Collections.singletonMap("errorCode", errorCode))
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}
