package com.vdc.pdi.algorithminlet.validator;

import com.vdc.pdi.algorithminlet.dto.request.AlarmEventRequest;
import com.vdc.pdi.algorithminlet.dto.request.StateStreamRequest;
import com.vdc.pdi.algorithminlet.exception.InletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 请求校验器测试
 */
@ExtendWith(MockitoExtension.class)
class RequestValidatorTest {

    @InjectMocks
    private RequestValidator requestValidator;

    @Test
    void validateStateStream_ValidStateCode_Success() {
        // Given - S3=3: 门关闭(0), 有人(1), 未进出(0)
        StateStreamRequest.StateTriple state = StateStreamRequest.StateTriple.builder()
                .doorOpen(0)
                .personPresent(1)
                .enteringExiting(0)
                .build();

        StateStreamRequest request = StateStreamRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .timestamp(LocalDateTime.now())
                .state(state)
                .stateCode(3)
                .build();

        // When & Then - 不应抛出异常
        assertDoesNotThrow(() -> requestValidator.validateStateStream(request));
    }

    @Test
    void validateStateStream_InvalidStateCode_ThrowsException() {
        // Given - 状态码与三元组不匹配
        StateStreamRequest.StateTriple state = StateStreamRequest.StateTriple.builder()
                .doorOpen(0)
                .personPresent(1)
                .enteringExiting(0)
                .build();

        StateStreamRequest request = StateStreamRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .timestamp(LocalDateTime.now())
                .state(state)
                .stateCode(5) // 期望应该是3
                .build();

        // When & Then
        InletException exception = assertThrows(InletException.class, () ->
                requestValidator.validateStateStream(request));
        assertTrue(exception.getMessage().contains("状态码不一致"));
    }

    @Test
    void validateStateStream_InvalidStateCombination_ThrowsException() {
        // Given - 无效的状态组合 (001: 门关闭, 无人, 进出中)
        StateStreamRequest.StateTriple state = StateStreamRequest.StateTriple.builder()
                .doorOpen(0)
                .personPresent(0)
                .enteringExiting(1)
                .build();

        StateStreamRequest request = StateStreamRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .timestamp(LocalDateTime.now())
                .state(state)
                .stateCode(1)
                .build();

        // When & Then
        InletException exception = assertThrows(InletException.class, () ->
                requestValidator.validateStateStream(request));
        assertTrue(exception.getMessage().contains("无效的状态组合"));
    }

    @Test
    void validateStateStream_FutureTimestamp_ThrowsException() {
        // Given - 未来时间戳
        StateStreamRequest.StateTriple state = StateStreamRequest.StateTriple.builder()
                .doorOpen(0)
                .personPresent(1)
                .enteringExiting(0)
                .build();

        StateStreamRequest request = StateStreamRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .timestamp(LocalDateTime.now().plusHours(1)) // 未来1小时
                .state(state)
                .stateCode(3)
                .build();

        // When & Then
        InletException exception = assertThrows(InletException.class, () ->
                requestValidator.validateStateStream(request));
        assertTrue(exception.getMessage().contains("时间戳不能是未来时间"));
    }

    @Test
    void validateStateStream_ExpiredTimestamp_ThrowsException() {
        // Given - 过期时间戳
        StateStreamRequest.StateTriple state = StateStreamRequest.StateTriple.builder()
                .doorOpen(0)
                .personPresent(1)
                .enteringExiting(0)
                .build();

        StateStreamRequest request = StateStreamRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .timestamp(LocalDateTime.now().minusMinutes(15)) // 15分钟前
                .state(state)
                .stateCode(3)
                .build();

        // When & Then
        InletException exception = assertThrows(InletException.class, () ->
                requestValidator.validateStateStream(request));
        assertTrue(exception.getMessage().contains("时间戳已过期"));
    }

    @Test
    void validateAlarmEvent_ValidUrl_Success() {
        // Given
        AlarmEventRequest request = AlarmEventRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .alarmType("SMOKE")
                .timestamp(LocalDateTime.now())
                .imageUrl("http://minio/bucket/alarm.jpg")
                .build();

        // When & Then - 不应抛出异常
        assertDoesNotThrow(() -> requestValidator.validateAlarmEvent(request));
    }

    @Test
    void validateAlarmEvent_InvalidUrl_ThrowsException() {
        // Given
        AlarmEventRequest request = AlarmEventRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .alarmType("SMOKE")
                .timestamp(LocalDateTime.now())
                .imageUrl("ftp://invalid/url.jpg") // 无效协议
                .build();

        // When & Then
        InletException exception = assertThrows(InletException.class, () ->
                requestValidator.validateAlarmEvent(request));
        assertTrue(exception.getMessage().contains("图片URL格式错误"));
    }

    @Test
    void validateAlarmEvent_HttpsUrl_Success() {
        // Given
        AlarmEventRequest request = AlarmEventRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .alarmType("SMOKE")
                .timestamp(LocalDateTime.now())
                .imageUrl("https://minio/bucket/alarm.jpg")
                .build();

        // When & Then - 不应抛出异常
        assertDoesNotThrow(() -> requestValidator.validateAlarmEvent(request));
    }

    @Test
    void validateAlarmEvent_EmptyUrl_Success() {
        // Given - 空URL是可选的
        AlarmEventRequest request = AlarmEventRequest.builder()
                .boxId("BOX_001")
                .channelId("CH_001")
                .alarmType("SMOKE")
                .timestamp(LocalDateTime.now())
                .imageUrl(null)
                .build();

        // When & Then - 不应抛出异常
        assertDoesNotThrow(() -> requestValidator.validateAlarmEvent(request));
    }
}
