package com.vdc.pdi.common.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SoftDeletable接口单元测试
 */
class SoftDeletableTest {

    // 简单的实现类用于测试
    static class TestSoftDeletable implements SoftDeletable {
        private LocalDateTime deletedAt;

        @Override
        public LocalDateTime getDeletedAt() {
            return deletedAt;
        }

        @Override
        public void setDeletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
        }
    }

    @Test
    void testSoftDelete() {
        TestSoftDeletable entity = new TestSoftDeletable();

        assertNull(entity.getDeletedAt());
        assertFalse(entity.isDeleted());

        entity.softDelete();

        assertNotNull(entity.getDeletedAt());
        assertTrue(entity.isDeleted());
    }

    @Test
    void testRestore() {
        TestSoftDeletable entity = new TestSoftDeletable();

        entity.softDelete();
        assertTrue(entity.isDeleted());

        entity.restore();

        assertNull(entity.getDeletedAt());
        assertFalse(entity.isDeleted());
    }

    @Test
    void testIsDeleted() {
        TestSoftDeletable entity = new TestSoftDeletable();

        assertFalse(entity.isDeleted());

        entity.setDeletedAt(LocalDateTime.now());
        assertTrue(entity.isDeleted());

        entity.setDeletedAt(null);
        assertFalse(entity.isDeleted());
    }
}
