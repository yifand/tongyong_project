package com.vdc.pdi.device.service;

import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.ChannelRepository;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import com.vdc.pdi.device.domain.vo.BoxStatus;
import com.vdc.pdi.device.dto.response.DeviceOverviewResponse;
import com.vdc.pdi.device.dto.response.SiteDeviceStatusResponse;
import com.vdc.pdi.device.service.impl.DeviceStatusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 设备状态服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class DeviceStatusServiceTest {

    @Mock
    private EdgeBoxRepository edgeBoxRepository;

    @Mock
    private ChannelRepository channelRepository;

    private DeviceStatusServiceImpl deviceStatusService;

    private EdgeBox onlineBox;
    private EdgeBox offlineBox;

    @BeforeEach
    void setUp() {
        // 手动创建Service实例，避免QueryDSL类加载问题
        deviceStatusService = new DeviceStatusServiceImpl(edgeBoxRepository, channelRepository);

        onlineBox = new EdgeBox();
        onlineBox.setId(1L);
        onlineBox.setSiteId(1L);
        onlineBox.setName("在线盒子");
        onlineBox.setStatus(1);
        onlineBox.setLastHeartbeatAt(LocalDateTime.now());
        onlineBox.setVersion("v2.1.0");
        onlineBox.setCpuUsage(45.5);
        onlineBox.setMemoryUsage(60.0);
        onlineBox.setDiskUsage(70.0);

        offlineBox = new EdgeBox();
        offlineBox.setId(2L);
        offlineBox.setSiteId(1L);
        offlineBox.setName("离线盒子");
        offlineBox.setStatus(0);
        offlineBox.setLastHeartbeatAt(LocalDateTime.now().minusMinutes(10));
        offlineBox.setVersion("v2.0.0");
    }

    @Test
    void getOverview_Success() {
        // Given
        when(edgeBoxRepository.countBySiteIdAndDeletedAtIsNull(1L)).thenReturn(2L);
        when(edgeBoxRepository.countBySiteIdAndStatusAndDeletedAtIsNull(1L, 1)).thenReturn(1L);
        when(channelRepository.countBySiteIdAndDeletedAtIsNull(1L)).thenReturn(4L);
        when(channelRepository.countBySiteIdAndStatusAndDeletedAtIsNull(1L, 1)).thenReturn(3L);
        when(channelRepository.countBySiteIdGroupByAlgorithmType(1L)).thenReturn(Arrays.asList(
                new Object[]{"smoke", 2L},
                new Object[]{"pdi_left_front", 1L},
                new Object[]{"pdi_left_rear", 1L}
        ));

        // When
        DeviceOverviewResponse result = deviceStatusService.getOverview(1L, 1L, false);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getSiteId());
        assertEquals(2, result.getTotalBoxes());
        assertEquals(1, result.getOnlineBoxes());
        assertEquals(1, result.getOfflineBoxes());
        assertEquals(50.0, result.getBoxOnlineRate());
        assertEquals(4, result.getTotalChannels());
        assertEquals(3, result.getOnlineChannels());
        assertEquals(2, result.getSmokeChannels());
    }

    @Test
    void getBoxStatus_Online() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(onlineBox));

        // When
        BoxStatus result = deviceStatusService.getBoxStatus(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getBoxId());
        assertTrue(result.getOnline());
        assertNotNull(result.getLastHeartbeatAt());
    }

    @Test
    void getBoxStatus_Offline() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(offlineBox));

        // When
        BoxStatus result = deviceStatusService.getBoxStatus(2L);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getBoxId());
        assertFalse(result.getOnline());
    }

    @Test
    void getBoxStatus_NotFound() {
        // Given
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        // When
        BoxStatus result = deviceStatusService.getBoxStatus(999L);

        // Then
        assertNull(result);
    }

    @Test
    void getAllSitesStatus_Success() {
        // Given
        List<EdgeBox> boxes = Arrays.asList(onlineBox, offlineBox);
        when(edgeBoxRepository.findAllByDeletedAtIsNull()).thenReturn(boxes);
        when(channelRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<SiteDeviceStatusResponse> result = deviceStatusService.getAllSitesStatus(1L, true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Both boxes in same site
        assertEquals(1L, result.get(0).getSiteId());
        assertEquals(2, result.get(0).getTotalBoxes());
        assertEquals(1, result.get(0).getOnlineBoxes());
        assertEquals(1, result.get(0).getOfflineBoxes());
        assertEquals(50.0, result.get(0).getBoxOnlineRate());
    }
}
