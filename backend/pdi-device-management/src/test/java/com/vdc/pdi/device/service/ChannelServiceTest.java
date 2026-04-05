package com.vdc.pdi.device.service;

import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.device.domain.entity.Channel;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.ChannelRepository;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import com.vdc.pdi.device.dto.request.ChannelRequest;
import com.vdc.pdi.device.dto.response.ChannelResponse;
import com.vdc.pdi.device.mapper.ChannelMapper;
import com.vdc.pdi.device.service.impl.ChannelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 通道服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private EdgeBoxRepository edgeBoxRepository;

    @Mock
    private ChannelMapper channelMapper;

    private ChannelServiceImpl channelService;

    private Channel testChannel;
    private EdgeBox testBox;
    private ChannelRequest testRequest;

    @BeforeEach
    void setUp() {
        // 手动创建Service实例，避免QueryDSL类加载问题
        channelService = new ChannelServiceImpl(channelRepository, edgeBoxRepository, channelMapper);

        testBox = new EdgeBox();
        testBox.setId(1L);
        testBox.setSiteId(1L);
        testBox.setName("测试盒子");
        testBox.setStatus(1);

        testChannel = new Channel();
        testChannel.setId(1L);
        testChannel.setBoxId(1L);
        testChannel.setSiteId(1L);
        testChannel.setName("测试通道");
        testChannel.setType(0);
        testChannel.setStatus(1);
        testChannel.setAlgorithmType("smoke");
        testChannel.setRtspUrl("rtsp://192.168.1.100/stream1");

        testRequest = new ChannelRequest();
        testRequest.setBoxId(1L);
        testRequest.setSiteId(1L);
        testRequest.setName("测试通道");
        testRequest.setType(0);
        testRequest.setAlgorithmType("smoke");
        testRequest.setRtspUrl("rtsp://192.168.1.100/stream1");
    }

    @Test
    void getChannel_Success() {
        // Given
        when(channelRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testChannel));
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testBox));
        ChannelResponse response = new ChannelResponse();
        response.setId(1L);
        response.setName("测试通道");
        when(channelMapper.toResponse(testChannel)).thenReturn(response);

        // When
        ChannelResponse result = channelService.getChannel(1L, null);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试通道", result.getName());
        verify(channelRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void getChannel_NotFound() {
        // Given
        when(channelRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            channelService.getChannel(1L, null);
        });
        assertEquals("通道不存在", exception.getMessage());
    }

    @Test
    void getChannel_WithSiteIsolation() {
        // Given
        testChannel.setSiteId(2L);
        when(channelRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testChannel));

        // When & Then - 用户尝试访问其他站点的通道
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            channelService.getChannel(1L, 1L);
        });
        assertEquals("无权访问该通道", exception.getMessage());
    }

    @Test
    void createChannel_Success() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testBox));
        when(channelMapper.toEntity(any(ChannelRequest.class))).thenReturn(testChannel);
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        // When
        Long channelId = channelService.createChannel(testRequest, null, 1L);

        // Then
        assertNotNull(channelId);
        assertEquals(1L, channelId);
        assertEquals(1L, testChannel.getSiteId());
        verify(channelRepository).save(any(Channel.class));
    }

    @Test
    void createChannel_BoxNotFound() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            channelService.createChannel(testRequest, null, 1L);
        });
        assertEquals("所属盒子不存在", exception.getMessage());
    }

    @Test
    void updateChannel_Success() {
        // Given
        when(channelRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testChannel));
        // 注意：当boxId未变更时，不会查询edgeBoxRepository
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        // Mock the mapper to actually update the entity
        doAnswer(invocation -> {
            ChannelRequest request = invocation.getArgument(0);
            Channel channel = invocation.getArgument(1);
            channel.setName(request.getName());
            return null;
        }).when(channelMapper).updateEntity(any(ChannelRequest.class), any(Channel.class));

        ChannelRequest updateRequest = new ChannelRequest();
        updateRequest.setBoxId(1L);
        updateRequest.setSiteId(1L);
        updateRequest.setName("新名称");
        updateRequest.setType(0);

        // When
        channelService.updateChannel(1L, updateRequest, null);

        // Then
        assertEquals("新名称", testChannel.getName());
        verify(channelRepository).save(testChannel);
    }

    @Test
    void deleteChannel_Success() {
        // Given
        when(channelRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testChannel));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        // When
        channelService.deleteChannel(1L, null);

        // Then
        assertNotNull(testChannel.getDeletedAt());
        verify(channelRepository).save(testChannel);
    }

    @Test
    void getChannelsByBoxId_Success() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testBox));
        List<Channel> channels = Arrays.asList(testChannel);
        when(channelRepository.findByBoxIdAndDeletedAtIsNull(1L)).thenReturn(channels);

        ChannelResponse response = new ChannelResponse();
        response.setId(1L);
        response.setName("测试通道");
        when(channelMapper.toResponseList(channels)).thenReturn(Arrays.asList(response));

        // When
        List<ChannelResponse> result = channelService.getChannelsByBoxId(1L, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试通道", result.get(0).getName());
    }

    @Test
    void getChannelsByBoxIdInternal_Success() {
        // Given
        List<Channel> channels = Arrays.asList(testChannel);
        when(channelRepository.findByBoxIdAndDeletedAtIsNull(1L)).thenReturn(channels);

        // When
        List<Channel> result = channelService.getChannelsByBoxIdInternal(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateChannelStatus_Success() {
        // Given
        when(channelRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testChannel));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        // When
        channelService.updateChannelStatus(1L, 0);

        // Then
        assertEquals(0, testChannel.getStatus());
        verify(channelRepository).save(testChannel);
    }

    @Test
    void getChannelById_Success() {
        // Given
        when(channelRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testChannel));

        // When
        Channel result = channelService.getChannelById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getChannelById_NotFound() {
        // Given
        when(channelRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When
        Channel result = channelService.getChannelById(1L);

        // Then
        assertNull(result);
    }
}
