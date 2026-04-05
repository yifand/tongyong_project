package com.vdc.pdi.device.task;

import com.vdc.pdi.device.service.BoxService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * 设备离线检测定时任务测试
 */
@ExtendWith(MockitoExtension.class)
class DeviceOfflineCheckTaskTest {

    @Mock
    private BoxService boxService;

    @InjectMocks
    private DeviceOfflineCheckTask deviceOfflineCheckTask;

    @Test
    void checkOfflineStatus_Success() {
        // When
        deviceOfflineCheckTask.checkOfflineStatus();

        // Then
        verify(boxService).checkAndUpdateOfflineStatus();
    }
}
