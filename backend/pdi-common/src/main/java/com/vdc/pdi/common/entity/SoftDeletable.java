package com.vdc.pdi.common.entity;

import java.time.LocalDateTime;

/**
 * 逻辑删除支持
 * 通过设置deletedAt字段实现软删除
 */
public interface SoftDeletable {

    /**
     * 获取删除时间
     */
    LocalDateTime getDeletedAt();

    /**
     * 设置删除时间
     */
    void setDeletedAt(LocalDateTime deletedAt);

    /**
     * 执行逻辑删除
     */
    default void softDelete() {
        setDeletedAt(LocalDateTime.now());
    }

    /**
     * 恢复逻辑删除
     */
    default void restore() {
        setDeletedAt(null);
    }

    /**
     * 是否已删除
     */
    default boolean isDeleted() {
        return getDeletedAt() != null;
    }
}
