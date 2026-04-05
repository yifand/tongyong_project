package com.vdc.pdi.algorithminlet.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 幂等记录实体
 * 用于防止重复处理同一条数据
 */
@Entity
@Table(name = "idempotency_record", indexes = {
    @Index(name = "idx_key_created", columnList = "record_key,created_at"),
    @Index(name = "idx_expire", columnList = "expire_at")
})
@Data
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 幂等键: boxId:channelId:timestamp
     */
    @Column(name = "record_key", length = 255, nullable = false, unique = true)
    private String key;

    /**
     * 记录类型: STATE_STREAM, ALARM_EVENT
     */
    @Column(name = "type", length = 32, nullable = false)
    private String type;

    /**
     * 过期时间
     */
    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
