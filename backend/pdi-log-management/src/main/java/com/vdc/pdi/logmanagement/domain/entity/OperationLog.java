package com.vdc.pdi.logmanagement.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 操作日志实体
 */
@Entity
@Table(name = "sys_operation_log", indexes = {
    @Index(name = "idx_op_log_site_id", columnList = "site_id"),
    @Index(name = "idx_op_log_user_id", columnList = "user_id"),
    @Index(name = "idx_op_log_type", columnList = "operation_type"),
    @Index(name = "idx_op_log_result", columnList = "result"),
    @Index(name = "idx_op_log_created", columnList = "created_at"),
    @Index(name = "idx_op_log_user_created", columnList = "user_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLog extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "operation_type", nullable = false)
    private Integer operationType;

    @Column(name = "operation_detail", length = 512)
    private String operationDetail;

    @Column(name = "request_params", length = 2048)
    private String requestParams;

    @Column(name = "result", nullable = false)
    private Integer result;

    @Column(name = "error_msg", length = 1024)
    private String errorMsg;

    @Column(name = "execution_time")
    private Long executionTime;
}
