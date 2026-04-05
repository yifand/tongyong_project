package com.vdc.pdi.device.integration;

import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EdgeBox Repository 集成测试
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class EdgeBoxRepositoryIntegrationTest {

    @Autowired
    private EdgeBoxRepository edgeBoxRepository;

    @Test
    void saveAndFind_Success() {
        // Given
        EdgeBox box = new EdgeBox();
        box.setSiteId(1L);
        box.setName("测试盒子");
        box.setIpAddress("192.168.1.100");
        box.setStatus(1);
        box.setVersion("v2.1.0");

        // When
        EdgeBox saved = edgeBoxRepository.save(box);
        Optional<EdgeBox> found = edgeBoxRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("测试盒子", found.get().getName());
        assertEquals("192.168.1.100", found.get().getIpAddress());
    }

    @Test
    void findByIdAndDeletedAtIsNull_Success() {
        // Given
        EdgeBox box = createTestBox("测试盒子1", "192.168.1.101");
        edgeBoxRepository.save(box);

        // When
        Optional<EdgeBox> found = edgeBoxRepository.findByIdAndDeletedAtIsNull(box.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("测试盒子1", found.get().getName());
    }

    @Test
    void findByIdAndDeletedAtIsNull_DeletedBox() {
        // Given
        EdgeBox box = createTestBox("已删除盒子", "192.168.1.102");
        box.setDeletedAt(LocalDateTime.now());
        edgeBoxRepository.save(box);

        // When
        Optional<EdgeBox> found = edgeBoxRepository.findByIdAndDeletedAtIsNull(box.getId());

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void findByIpAddressAndDeletedAtIsNull_Success() {
        // Given
        EdgeBox box = createTestBox("测试盒子", "192.168.1.103");
        edgeBoxRepository.save(box);

        // When
        Optional<EdgeBox> found = edgeBoxRepository.findByIpAddressAndDeletedAtIsNull("192.168.1.103");

        // Then
        assertTrue(found.isPresent());
        assertEquals("测试盒子", found.get().getName());
    }

    @Test
    void findBySiteIdAndDeletedAtIsNull_Success() {
        // Given
        edgeBoxRepository.save(createTestBox("盒子1", "192.168.1.104", 1L));
        edgeBoxRepository.save(createTestBox("盒子2", "192.168.1.105", 1L));
        edgeBoxRepository.save(createTestBox("盒子3", "192.168.1.106", 2L));

        // When
        List<EdgeBox> result = edgeBoxRepository.findBySiteIdAndDeletedAtIsNull(1L);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void findBySiteIdAndStatusAndDeletedAtIsNull_Success() {
        // Given
        edgeBoxRepository.save(createTestBoxWithStatus("在线盒子", "192.168.1.107", 1L, 1));
        edgeBoxRepository.save(createTestBoxWithStatus("离线盒子", "192.168.1.108", 1L, 0));

        // When
        List<EdgeBox> onlineBoxes = edgeBoxRepository.findBySiteIdAndStatusAndDeletedAtIsNull(1L, 1);
        List<EdgeBox> offlineBoxes = edgeBoxRepository.findBySiteIdAndStatusAndDeletedAtIsNull(1L, 0);

        // Then
        assertEquals(1, onlineBoxes.size());
        assertEquals(1, offlineBoxes.size());
    }

    @Test
    void countBySiteIdAndStatusAndDeletedAtIsNull_Success() {
        // Given
        edgeBoxRepository.save(createTestBoxWithStatus("盒子1", "192.168.1.110", 1L, 1));
        edgeBoxRepository.save(createTestBoxWithStatus("盒子2", "192.168.1.111", 1L, 1));
        edgeBoxRepository.save(createTestBoxWithStatus("盒子3", "192.168.1.112", 1L, 0));

        // When
        long onlineCount = edgeBoxRepository.countBySiteIdAndStatusAndDeletedAtIsNull(1L, 1);
        long offlineCount = edgeBoxRepository.countBySiteIdAndStatusAndDeletedAtIsNull(1L, 0);

        // Then
        assertEquals(2, onlineCount);
        assertEquals(1, offlineCount);
    }

    @Test
    void findTimeoutBoxes_Success() {
        // Given
        EdgeBox box = createTestBoxWithStatus("盒子", "192.168.1.113", 1L, 1);
        box.setLastHeartbeatAt(LocalDateTime.now().minusMinutes(10));
        edgeBoxRepository.save(box);

        // When
        List<EdgeBox> result = edgeBoxRepository.findTimeoutBoxes(
                LocalDateTime.now().minusMinutes(5));

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void findByNameContainingAndDeletedAtIsNull_Success() {
        // Given
        edgeBoxRepository.save(createTestBox("测试盒子A", "192.168.2.1"));
        edgeBoxRepository.save(createTestBox("测试盒子B", "192.168.2.2"));
        edgeBoxRepository.save(createTestBox("生产盒子", "192.168.2.3"));

        // When
        List<EdgeBox> result = edgeBoxRepository.findByNameContainingAndDeletedAtIsNull("测试");

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void findAllByDeletedAtIsNull_Success() {
        // Given
        edgeBoxRepository.save(createTestBox("盒子1", "192.168.2.4"));
        edgeBoxRepository.save(createTestBox("盒子2", "192.168.2.5"));
        EdgeBox deleted = createTestBox("已删除", "192.168.2.6");
        deleted.setDeletedAt(LocalDateTime.now());
        edgeBoxRepository.save(deleted);

        // When
        List<EdgeBox> result = edgeBoxRepository.findAllByDeletedAtIsNull();

        // Then
        assertEquals(2, result.size());
    }

    private EdgeBox createTestBox(String name, String ip) {
        return createTestBox(name, ip, 1L);
    }

    private EdgeBox createTestBox(String name, String ip, Long siteId) {
        EdgeBox box = new EdgeBox();
        box.setSiteId(siteId);
        box.setName(name);
        box.setIpAddress(ip);
        box.setStatus(1);
        box.setVersion("v2.1.0");
        return box;
    }

    private EdgeBox createTestBoxWithStatus(String name, String ip, Long siteId, Integer status) {
        EdgeBox box = createTestBox(name, ip, siteId);
        box.setStatus(status);
        return box;
    }
}
