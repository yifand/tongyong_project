package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体（分区表）
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 日志时间（分区键）
     */
    private LocalDateTime logTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作描述
     */
    private String operation;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 执行时间(毫秒)
     */
    private Integer executeTime;

    /**
     * 状态: 0-失败, 1-成功
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

}
