package com.vdc.pdi.common.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseEntity单元测试
 */
class BaseEntityTest {

    // 简单的实现类用于测试
    static class TestEntity extends BaseEntity {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void testIdGetterSetter() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getId());

        entity.setId(1L);
        assertEquals(1L, entity.getId());
    }

    @Test
    void testSiteIdGetterSetter() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getSiteId());

        entity.setSiteId(100L);
        assertEquals(100L, entity.getSiteId());
    }

    @Test
    void testCreatedAtGetterSetter() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getCreatedAt());

        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterSetter() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getUpdatedAt());

        LocalDateTime now = LocalDateTime.now();
        entity.setUpdatedAt(now);
        assertEquals(now, entity.getUpdatedAt());
    }

    @Test
    void testDeletedAtGetterSetter() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getDeletedAt());

        LocalDateTime now = LocalDateTime.now();
        entity.setDeletedAt(now);
        assertEquals(now, entity.getDeletedAt());
    }

    @Test
    void testCreatedByGetterSetter() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getCreatedBy());

        entity.setCreatedBy(1000L);
        assertEquals(1000L, entity.getCreatedBy());
    }

    @Test
    void testIsDeleted() {
        TestEntity entity = new TestEntity();

        // 初始状态
        assertFalse(entity.isDeleted());

        // 设置删除时间
        entity.setDeletedAt(LocalDateTime.now());
        assertTrue(entity.isDeleted());

        // 恢复删除
        entity.setDeletedAt(null);
        assertFalse(entity.isDeleted());
    }

    @Test
    void testSoftDeleteLogic() {
        TestEntity entity = new TestEntity();

        assertNull(entity.getDeletedAt());
        assertFalse(entity.isDeleted());

        // 模拟逻辑删除
        LocalDateTime deleteTime = LocalDateTime.now();
        entity.setDeletedAt(deleteTime);

        assertEquals(deleteTime, entity.getDeletedAt());
        assertTrue(entity.isDeleted());
    }
}
