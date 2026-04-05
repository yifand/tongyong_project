package com.vdc.pdi.device.integration;

import com.vdc.pdi.device.domain.entity.Channel;
import com.vdc.pdi.device.domain.repository.ChannelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Channel Repository 集成测试
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class ChannelRepositoryIntegrationTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Test
    void saveAndFind_Success() {
        // Given
        Channel channel = new Channel();
        channel.setBoxId(1L);
        channel.setSiteId(1L);
        channel.setName("测试通道");
        channel.setType(0);
        channel.setStatus(1);
        channel.setAlgorithmType("smoke");
        channel.setRtspUrl("rtsp://192.168.1.100/stream1");

        // When
        Channel saved = channelRepository.save(channel);
        Optional<Channel> found = channelRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("测试通道", found.get().getName());
        assertEquals("smoke", found.get().getAlgorithmType());
    }

    @Test
    void findByIdAndDeletedAtIsNull_Success() {
        // Given
        Channel channel = createTestChannel("测试通道1");
        channelRepository.save(channel);

        // When
        Optional<Channel> found = channelRepository.findByIdAndDeletedAtIsNull(channel.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("测试通道1", found.get().getName());
    }

    @Test
    void findByIdAndDeletedAtIsNull_DeletedChannel() {
        // Given
        Channel channel = createTestChannel("已删除通道");
        channel.setDeletedAt(LocalDateTime.now());
        channelRepository.save(channel);

        // When
        Optional<Channel> found = channelRepository.findByIdAndDeletedAtIsNull(channel.getId());

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void findByBoxIdAndDeletedAtIsNull_Success() {
        // Given
        channelRepository.save(createTestChannelWithBoxId("通道1", 1L));
        channelRepository.save(createTestChannelWithBoxId("通道2", 1L));
        channelRepository.save(createTestChannelWithBoxId("通道3", 2L));

        // When
        List<Channel> result = channelRepository.findByBoxIdAndDeletedAtIsNull(1L);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void findBySiteIdAndDeletedAtIsNull_Success() {
        // Given
        channelRepository.save(createTestChannelWithSiteId("通道1", 1L));
        channelRepository.save(createTestChannelWithSiteId("通道2", 1L));
        channelRepository.save(createTestChannelWithSiteId("通道3", 2L));

        // When
        List<Channel> result = channelRepository.findBySiteIdAndDeletedAtIsNull(1L);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void findByAlgorithmTypeAndDeletedAtIsNull_Success() {
        // Given
        channelRepository.save(createTestChannelWithAlgorithm("烟雾检测通道", "smoke"));
        channelRepository.save(createTestChannelWithAlgorithm("左前门通道", "pdi_left_front"));
        channelRepository.save(createTestChannelWithAlgorithm("烟雾检测通道2", "smoke"));

        // When
        List<Channel> smokeChannels = channelRepository.findByAlgorithmTypeAndDeletedAtIsNull("smoke");

        // Then
        assertEquals(2, smokeChannels.size());
    }

    @Test
    void findBySiteIdAndAlgorithmTypeAndDeletedAtIsNull_Success() {
        // Given
        channelRepository.save(createTestChannelWithSiteAndAlgorithm("通道1", 1L, "smoke"));
        channelRepository.save(createTestChannelWithSiteAndAlgorithm("通道2", 1L, "smoke"));
        channelRepository.save(createTestChannelWithSiteAndAlgorithm("通道3", 2L, "smoke"));

        // When
        List<Channel> result = channelRepository.findBySiteIdAndAlgorithmTypeAndDeletedAtIsNull(1L, "smoke");

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void countByBoxIdAndDeletedAtIsNull_Success() {
        // Given
        channelRepository.save(createTestChannelWithBoxId("通道1", 1L));
        channelRepository.save(createTestChannelWithBoxId("通道2", 1L));
        channelRepository.save(createTestChannelWithBoxId("通道3", 2L));

        // When
        long count = channelRepository.countByBoxIdAndDeletedAtIsNull(1L);

        // Then
        assertEquals(2, count);
    }

    @Test
    void countBySiteIdAndStatusAndDeletedAtIsNull_Success() {
        // Given
        channelRepository.save(createTestChannelWithSiteAndStatus("通道1", 1L, 1));
        channelRepository.save(createTestChannelWithSiteAndStatus("通道2", 1L, 1));
        channelRepository.save(createTestChannelWithSiteAndStatus("通道3", 1L, 0));

        // When
        long onlineCount = channelRepository.countBySiteIdAndStatusAndDeletedAtIsNull(1L, 1);
        long offlineCount = channelRepository.countBySiteIdAndStatusAndDeletedAtIsNull(1L, 0);

        // Then
        assertEquals(2, onlineCount);
        assertEquals(1, offlineCount);
    }

    @Test
    void findByNameContainingAndDeletedAtIsNull_Success() {
        // Given
        channelRepository.save(createTestChannel("测试通道A"));
        channelRepository.save(createTestChannel("测试通道B"));
        channelRepository.save(createTestChannel("生产通道"));

        // When
        List<Channel> result = channelRepository.findByNameContainingAndDeletedAtIsNull("测试");

        // Then
        assertEquals(2, result.size());
    }

    private Channel createTestChannel(String name) {
        Channel channel = new Channel();
        channel.setBoxId(1L);
        channel.setSiteId(1L);
        channel.setName(name);
        channel.setType(0);
        channel.setStatus(1);
        channel.setAlgorithmType("smoke");
        channel.setRtspUrl("rtsp://192.168.1.100/stream1");
        return channel;
    }

    private Channel createTestChannelWithBoxId(String name, Long boxId) {
        Channel channel = createTestChannel(name);
        channel.setBoxId(boxId);
        return channel;
    }

    private Channel createTestChannelWithSiteId(String name, Long siteId) {
        Channel channel = createTestChannel(name);
        channel.setSiteId(siteId);
        return channel;
    }

    private Channel createTestChannelWithAlgorithm(String name, String algorithmType) {
        Channel channel = createTestChannel(name);
        channel.setAlgorithmType(algorithmType);
        return channel;
    }

    private Channel createTestChannelWithSiteAndAlgorithm(String name, Long siteId, String algorithmType) {
        Channel channel = createTestChannel(name);
        channel.setSiteId(siteId);
        channel.setAlgorithmType(algorithmType);
        return channel;
    }

    private Channel createTestChannelWithSiteAndStatus(String name, Long siteId, Integer status) {
        Channel channel = createTestChannel(name);
        channel.setSiteId(siteId);
        channel.setStatus(status);
        return channel;
    }
}
