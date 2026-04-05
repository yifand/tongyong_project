package com.vdc.pdi.algorithminlet.service;

import com.vdc.pdi.algorithminlet.domain.event.HeartbeatEvent;
import com.vdc.pdi.algorithminlet.dto.request.HeartbeatRequest;
import com.vdc.pdi.algorithminlet.service.impl.HeartbeatServiceImpl;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 心跳服务实现测试
 */
@ExtendWith(MockitoExtension.class)
class HeartbeatServiceImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EdgeBoxRepository edgeBoxRepository;

    @InjectMocks
    private HeartbeatServiceImpl heartbeatService;

    @Test
    void processHeartbeat_Success() {
        // Given
        String boxId = "BOX_001";
        HeartbeatRequest request = HeartbeatRequest.builder()
                .boxId(boxId)
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .memoryUsage(62.1)
                .diskUsage(78.5)
                .version("v2.1.0")
                .build();

        EdgeBox box = new EdgeBox();
        box.setId(1L);
        box.setSiteId(1L);
        box.setName("Test Box");

        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(box));
        when(edgeBoxRepository.save(any(EdgeBox.class)))
                .thenReturn(box);

        // When
        heartbeatService.processHeartbeat(request, 1L);

        // Then
        ArgumentCaptor<EdgeBox> boxCaptor = ArgumentCaptor.forClass(EdgeBox.class);
        verify(edgeBoxRepository).save(boxCaptor.capture());

        EdgeBox savedBox = boxCaptor.getValue();
        assertEquals(1, savedBox.getStatus());
        assertEquals(45.2, savedBox.getCpuUsage());
        assertEquals(62.1, savedBox.getMemoryUsage());
        assertEquals(78.5, savedBox.getDiskUsage());
        assertEquals("v2.1.0", savedBox.getVersion());
        assertNotNull(savedBox.getLastHeartbeatAt());

        verify(eventPublisher).publishEvent(any(HeartbeatEvent.class));
    }

    @Test
    void processHeartbeat_BoxNotFound() {
        // Given
        String boxId = "BOX_999";
        HeartbeatRequest request = HeartbeatRequest.builder()
                .boxId(boxId)
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .memoryUsage(62.1)
                .diskUsage(78.5)
                .build();

        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(Exception.class, () ->
                heartbeatService.processHeartbeat(request, 1L));
        assertTrue(exception.getMessage().contains("盒子不存在"));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void processHeartbeat_OnlineStatusSet() {
        // Given
        EdgeBox box = new EdgeBox();
        box.setId(1L);
        box.setSiteId(1L);
        box.setStatus(0); // 离线状态

        HeartbeatRequest request = HeartbeatRequest.builder()
                .boxId("BOX_001")
                .timestamp(LocalDateTime.now())
                .cpuUsage(30.0)
                .memoryUsage(50.0)
                .diskUsage(60.0)
                .build();

        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(box));
        when(edgeBoxRepository.save(any(EdgeBox.class)))
                .thenReturn(box);

        // When
        heartbeatService.processHeartbeat(request, 1L);

        // Then
        ArgumentCaptor<EdgeBox> captor = ArgumentCaptor.forClass(EdgeBox.class);
        verify(edgeBoxRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getStatus()); // 状态应变为在线
    }
}
