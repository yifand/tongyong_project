package com.vdc.pdi.logmanagement.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 系统日志实体
 */
@Entity
@Table(name = "system_log", indexes = {
    @Index(name = "idx_sys_log_level", columnList = "level"),
    @Index(name = "idx_sys_log_module", columnList = "module"),
    @Index(name = "idx_sys_log_created", columnList = "created_at"),
    @Index(name = "idx_sys_log_level_created", columnList = "level, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "module", nullable = false, length = 64)
    private String module;

    @Column(name = "message", nullable = false, length = 2048)
    private String message;

    @Column(name = "stack_trace", length = 4096)
    private String stackTrace;

    @Column(name = "source_class", length = 256)
    private String sourceClass;

    @Column(name = "source_method", length = 128)
    private String sourceMethod;

    @Column(name = "thread_name", length = 64)
    private String threadName;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
