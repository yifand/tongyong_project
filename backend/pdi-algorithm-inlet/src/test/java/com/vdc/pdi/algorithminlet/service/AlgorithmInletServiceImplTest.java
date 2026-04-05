package com.vdc.pdi.algorithminlet.service;

import com.vdc.pdi.algorithminlet.domain.entity.IdempotencyRecord;
import com.vdc.pdi.algorithminlet.domain.event.AlarmEvent;
import com.vdc.pdi.algorithminlet.domain.event.StateStreamEvent;
import com.vdc.pdi.algorithminlet.domain.repository.IdempotencyRepository;
import com.vdc.pdi.algorithminlet.dto.request.AlarmEventRequest;
import com.vdc.pdi.algorithminlet.dto.request.StateStreamRequest;
import com.vdc.pdi.algorithminlet.service.impl.AlgorithmInletServiceImpl;
import com.vdc.pdi.algorithminlet.validator.RequestValidator;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 算法数据入口服务实现测试
 */
@ExtendWith(MockitoExtension.class)
class AlgorithmInletServiceImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private EdgeBoxRepository edgeBoxRepository;

    @InjectMocks
    private AlgorithmInletServiceImpl inletService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inletService, "idempotencyWindowMinutes", 5);
        ReflectionTestUtils.setField(inletService, "idempotencyExpireMinutes", 10);
    }

    @Test
    void processStateStream_Success() {
        // Given
        String boxId = "BOX_001";
        String channelId = "CH_001";
        LocalDateTime timestamp = LocalDateTime.now();

        StateStreamRequest.StateTriple state = StateStreamRequest.StateTriple.builder()
                .doorOpen(0)
                .personPresent(1)
                .enteringExiting(0)
                .build();

        StateStreamRequest request = StateStreamRequest.builder()
                .boxId(boxId)
                .channelId(channelId)
                .timestamp(timestamp)
                .state(state)
                .stateCode(3)
                .build();

        EdgeBox box = new EdgeBox();
        box.setId(1L);
        box.setSiteId(1L);

        when(idempotencyRepository.existsByKeyAndCreatedAtAfter(anyString(), any()))
                .thenReturn(false);
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(box));

        // When
        inletService.processStateStream(request, 1L);

        // Then
        verify(idempotencyRepository).save(any(IdempotencyRecord.class));
        verify(requestValidator).validateStateStream(request);
        verify(eventPublisher).publishEvent(any(StateStreamEvent.class));
    }

    @Test
    void processStateStream_DuplicateData_Ignored() {
        // Given
        String boxId = "BOX_001";
        String channelId = "CH_001";
        LocalDateTime timestamp = LocalDateTime.now();

        StateStreamRequest.StateTriple state = StateStreamRequest.StateTriple.builder()
                .doorOpen(0)
                .personPresent(1)
                .enteringExiting(0)
                .build();

        StateStreamRequest request = StateStreamRequest.builder()
                .boxId(boxId)
                .channelId(channelId)
                .timestamp(timestamp)
                .state(state)
                .stateCode(3)
                .build();

        when(idempotencyRepository.existsByKeyAndCreatedAtAfter(anyString(), any()))
                .thenReturn(true);

        // When
        inletService.processStateStream(request, 1L);

        // Then
        verify(idempotencyRepository, never()).save(any());
        verify(requestValidator, never()).validateStateStream(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void processAlarmEvent_Success() {
        // Given
        String boxId = "BOX_001";
        String channelId = "CH_001";
        LocalDateTime timestamp = LocalDateTime.now();

        AlarmEventRequest request = AlarmEventRequest.builder()
                .boxId(boxId)
                .channelId(channelId)
                .alarmType("SMOKE")
                .timestamp(timestamp)
                .imageUrl("http://minio/alarm.jpg")
                .confidence(0.92)
                .location("休息区A")
                .build();

        EdgeBox box = new EdgeBox();
        box.setId(1L);
        box.setSiteId(1L);

        when(idempotencyRepository.existsByKeyAndCreatedAtAfter(anyString(), any()))
                .thenReturn(false);
        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(box));

        // When
        inletService.processAlarmEvent(request, 1L);

        // Then
        ArgumentCaptor<IdempotencyRecord> captor = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(idempotencyRepository).save(captor.capture());
        assertEquals("ALARM_EVENT", captor.getValue().getType());

        verify(requestValidator).validateAlarmEvent(request);
        verify(eventPublisher).publishEvent(any(AlarmEvent.class));
    }

    @Test
    void processAlarmEvent_DuplicateAlarm_Ignored() {
        // Given
        AlarmEventRequest request = AlarmEventRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .alarmType("SMOKE")
                .timestamp(LocalDateTime.now())
                .build();

        when(idempotencyRepository.existsByKeyAndCreatedAtAfter(anyString(), any()))
                .thenReturn(true);

        // When
        inletService.processAlarmEvent(request, 1L);

        // Then
        verify(idempotencyRepository, never()).save(any());
        verify(requestValidator, never()).validateAlarmEvent(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
