package com.vdc.pdi.device.service;

import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.common.enums.ResultCode;
import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.device.config.DeviceConfig;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import com.vdc.pdi.device.domain.vo.HeartbeatInfo;
import com.vdc.pdi.device.dto.request.BoxQueryRequest;
import com.vdc.pdi.device.dto.request.BoxRequest;
import com.vdc.pdi.device.dto.response.BoxResponse;
import com.vdc.pdi.device.mapper.BoxMapper;
import com.vdc.pdi.device.service.impl.BoxServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 盒子服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private EdgeBoxRepository edgeBoxRepository;

    @Mock
    private BoxMapper boxMapper;

    @Mock
    private DeviceConfig deviceConfig;

    @Mock
    private ChannelService channelService;

    private BoxServiceImpl boxService;

    private EdgeBox testBox;
    private BoxRequest testRequest;

    @BeforeEach
    void setUp() {
        // 手动创建Service实例，避免QueryDSL类加载问题
        boxService = new BoxServiceImpl(edgeBoxRepository, boxMapper, deviceConfig, channelService);

        testBox = new EdgeBox();
        testBox.setId(1L);
        testBox.setSiteId(1L);
        testBox.setName("测试盒子");
        testBox.setIpAddress("192.168.1.100");
        testBox.setStatus(1);
        testBox.setVersion("v2.1.0");

        testRequest = new BoxRequest();
        testRequest.setName("测试盒子");
        testRequest.setIpAddress("192.168.1.100");
        testRequest.setSiteId(1L);
        testRequest.setVersion("v2.1.0");
    }

    @Test
    void getBox_Success() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testBox));
        BoxResponse response = new BoxResponse();
        response.setId(1L);
        when(boxMapper.toResponse(testBox)).thenReturn(response);

        // When
        BoxResponse result = boxService.getBox(1L, null);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(edgeBoxRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void getBox_NotFound() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            boxService.getBox(1L, null);
        });
        assertEquals("盒子不存在", exception.getMessage());
    }

    @Test
    void createBox_Success() {
        // Given
        when(edgeBoxRepository.findByIpAddressAndDeletedAtIsNull(anyString())).thenReturn(Optional.empty());
        when(boxMapper.toEntity(any(BoxRequest.class))).thenReturn(testBox);
        when(edgeBoxRepository.save(any(EdgeBox.class))).thenReturn(testBox);

        // When
        Long boxId = boxService.createBox(testRequest, null, 1L);

        // Then
        assertNotNull(boxId);
        assertEquals(1L, boxId);
        verify(edgeBoxRepository).save(any(EdgeBox.class));
    }

    @Test
    void createBox_IpExists() {
        // Given
        when(edgeBoxRepository.findByIpAddressAndDeletedAtIsNull(anyString())).thenReturn(Optional.of(testBox));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            boxService.createBox(testRequest, null, 1L);
        });
        assertEquals("IP地址已存在", exception.getMessage());
    }

    @Test
    void updateHeartbeat_Success() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testBox));
        when(edgeBoxRepository.save(any(EdgeBox.class))).thenReturn(testBox);

        HeartbeatInfo heartbeat = HeartbeatInfo.builder()
                .boxId(1L)
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.5)
                .memoryUsage(62.0)
                .diskUsage(78.5)
                .version("v2.1.1")
                .build();

        // When
        boxService.updateHeartbeat(1L, heartbeat);

        // Then
        assertEquals(1, testBox.getStatus());
        assertEquals(45.5, testBox.getCpuUsage());
        assertEquals(62.0, testBox.getMemoryUsage());
        assertEquals(78.5, testBox.getDiskUsage());
        assertEquals("v2.1.1", testBox.getVersion());
        verify(edgeBoxRepository).save(testBox);
    }

    @Test
    void updateHeartbeat_BoxNotFound() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        HeartbeatInfo heartbeat = HeartbeatInfo.builder()
                .boxId(1L)
                .timestamp(LocalDateTime.now())
                .build();

        // When
        boxService.updateHeartbeat(1L, heartbeat);

        // Then
        verify(edgeBoxRepository, never()).save(any());
    }

    @Test
    void deleteBox_Success() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testBox));
        when(edgeBoxRepository.save(any(EdgeBox.class))).thenReturn(testBox);
        when(channelService.getChannelsByBoxIdInternal(1L)).thenReturn(Collections.emptyList());

        // When
        boxService.deleteBox(1L, null);

        // Then
        assertNotNull(testBox.getDeletedAt());
        verify(edgeBoxRepository).save(testBox);
    }
}
