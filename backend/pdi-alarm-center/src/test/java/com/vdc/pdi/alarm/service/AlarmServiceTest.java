package com.vdc.pdi.alarm.service;

import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import com.vdc.pdi.alarm.domain.repository.AlarmQueryRepository;
import com.vdc.pdi.alarm.domain.repository.AlarmRepository;
import com.vdc.pdi.alarm.domain.vo.AlarmStatisticsVO;
import com.vdc.pdi.alarm.dto.request.AlarmHistoryRequest;
import com.vdc.pdi.alarm.dto.request.AlarmProcessRequest;
import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.dto.response.AlarmStatisticsResponse;
import com.vdc.pdi.alarm.mapper.AlarmMapper;
import com.vdc.pdi.alarm.service.impl.AlarmServiceImpl;
import com.vdc.pdi.common.enums.AlarmStatusEnum;
import com.vdc.pdi.common.enums.AlarmTypeEnum;
import com.vdc.pdi.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 报警服务测试
 */
@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private AlarmQueryRepository alarmQueryRepository;

    @Mock
    private AlarmMapper alarmMapper;

    @Mock
    private AlarmSSEService alarmSSEService;

    @InjectMocks
    private AlarmServiceImpl alarmService;

    @BeforeEach
    void setUp() {
        // 初始化设置
    }

    @Nested
    @DisplayName("实时预警列表查询")
    class RealtimeAlarmTests {

        @Test
        @DisplayName("应返回最近N条报警")
        void shouldReturnRecentAlarms() {
            // Given
            List<AlarmRecord> mockAlarms = List.of(
                    createAlarmRecord(1L, AlarmTypeEnum.SMOKE.getCode(), 1L),
                    createAlarmRecord(2L, AlarmTypeEnum.PDI_VIOLATION.getCode(), 1L)
            );
            when(alarmQueryRepository.findRecentAlarms(anySet(), isNull(), eq(50)))
                    .thenReturn(mockAlarms);
            when(alarmMapper.toResponse(any())).thenReturn(new AlarmResponse());

            // When
            List<AlarmResponse> result = alarmService.getRealtimeAlarms(50, null);

            // Then
            assertThat(result).hasSize(2);
            verify(alarmQueryRepository).findRecentAlarms(anySet(), isNull(), eq(50));
        }

        @Test
        @DisplayName("应按类型筛选报警")
        void shouldFilterByType() {
            // Given
            when(alarmQueryRepository.findRecentAlarms(anySet(), eq(0), eq(50)))
                    .thenReturn(Collections.emptyList());

            // When
            alarmService.getRealtimeAlarms(50, 0);

            // Then
            verify(alarmQueryRepository).findRecentAlarms(anySet(), eq(0), eq(50));
        }
    }

    @Nested
    @DisplayName("历史预警分页查询")
    class HistoryAlarmTests {

        @Test
        @DisplayName("应支持多条件组合查询")
        void shouldSupportComplexQuery() {
            // Given
            AlarmHistoryRequest request = new AlarmHistoryRequest();
            request.setPage(1);
            request.setSize(20);
            request.setSiteId(1L);
            request.setType(0);
            request.setStatus(0);
            request.setStartTime(LocalDateTime.now().minusDays(7));
            request.setEndTime(LocalDateTime.now());

            Page<AlarmRecord> mockPage = new PageImpl<>(Collections.emptyList());
            when(alarmQueryRepository.findAll(any(), any()))
                    .thenReturn(mockPage);

            // When
            var result = alarmService.getHistoryAlarms(request);

            // Then
            assertThat(result).isNotNull();
            verify(alarmQueryRepository).findAll(any(), any());
        }
    }

    @Nested
    @DisplayName("报警详情查询")
    class AlarmDetailTests {

        @Test
        @DisplayName("应返回报警详情")
        void shouldReturnAlarmDetail() {
            // Given
            Long alarmId = 1L;
            AlarmRecord alarm = createAlarmRecord(alarmId, 0, 1L);
            when(alarmRepository.findById(alarmId)).thenReturn(Optional.of(alarm));
            when(alarmMapper.toResponse(alarm)).thenReturn(new AlarmResponse());

            // When
            AlarmResponse result = alarmService.getAlarmDetail(alarmId);

            // Then
            assertThat(result).isNotNull();
            verify(alarmRepository).findById(alarmId);
        }

        @Test
        @DisplayName("报警不存在时应抛出异常")
        void shouldThrowExceptionWhenAlarmNotFound() {
            // Given
            Long alarmId = 999L;
            when(alarmRepository.findById(alarmId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> alarmService.getAlarmDetail(alarmId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("报警记录不存在");
        }
    }

    @Nested
    @DisplayName("报警状态更新")
    class AlarmStatusUpdateTests {

        @Test
        @DisplayName("应成功标记报警为已处理")
        void shouldProcessAlarmSuccessfully() {
            // Given
            Long alarmId = 1L;
            AlarmRecord alarm = createAlarmRecord(alarmId, 0, 1L);
            alarm.setStatus(AlarmStatusEnum.UNPROCESSED.getCode());

            when(alarmRepository.findById(alarmId)).thenReturn(Optional.of(alarm));
            when(alarmRepository.save(any())).thenReturn(alarm);

            // When
            alarmService.processAlarm(alarmId, null);

            // Then
            assertThat(alarm.getStatus()).isEqualTo(AlarmStatusEnum.PROCESSED.getCode());
            assertThat(alarm.getProcessedAt()).isNotNull();
            verify(alarmRepository).save(alarm);
        }

        @Test
        @DisplayName("已处理报警不应重复处理")
        void shouldNotProcessAlreadyProcessedAlarm() {
            // Given
            Long alarmId = 1L;
            AlarmRecord alarm = createAlarmRecord(alarmId, 0, 1L);
            alarm.setStatus(AlarmStatusEnum.PROCESSED.getCode());

            when(alarmRepository.findById(alarmId)).thenReturn(Optional.of(alarm));

            // When & Then
            assertThatThrownBy(() -> alarmService.processAlarm(alarmId, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已处理");
        }

        @Test
        @DisplayName("应成功标记报警为误报")
        void shouldMarkFalsePositiveSuccessfully() {
            // Given
            Long alarmId = 1L;
            AlarmRecord alarm = createAlarmRecord(alarmId, 0, 1L);

            when(alarmRepository.findById(alarmId)).thenReturn(Optional.of(alarm));
            when(alarmRepository.save(any())).thenReturn(alarm);

            AlarmProcessRequest request = new AlarmProcessRequest();
            request.setRemark("经核实为误报");

            // When
            alarmService.markFalsePositive(alarmId, request);

            // Then
            assertThat(alarm.getStatus()).isEqualTo(AlarmStatusEnum.FALSE_POSITIVE.getCode());
            assertThat(alarm.getRemark()).isEqualTo("经核实为误报");
        }
    }

    @Nested
    @DisplayName("报警创建")
    class AlarmCreateTests {

        @Test
        @DisplayName("应成功创建报警并推送SSE")
        void shouldCreateAlarmAndPushSSE() {
            // Given
            AlarmCreateEvent event = AlarmCreateEvent.builder()
                    .type(0)
                    .siteId(1L)
                    .channelId(101L)
                    .alarmTime(LocalDateTime.now())
                    .location("金桥 - 休息区A")
                    .faceImageUrl("http://minio/face/xxx.jpg")
                    .sceneImageUrl("http://minio/scene/xxx.jpg")
                    .extraInfo("{\"confidence\": 0.92}")
                    .build();

            AlarmRecord savedAlarm = createAlarmRecord(1L, 0, 1L);
            when(alarmRepository.save(any(AlarmRecord.class))).thenReturn(savedAlarm);
            when(alarmMapper.toResponse(any())).thenReturn(new AlarmResponse());

            // When
            AlarmRecord result = alarmService.createAlarm(event);

            // Then
            assertThat(result).isNotNull();
            verify(alarmRepository).save(any(AlarmRecord.class));
            verify(alarmSSEService).pushAlarm(any(AlarmResponse.class));
        }
    }

    @Nested
    @DisplayName("今日统计")
    class TodayStatisticsTests {

        @Test
        @DisplayName("应正确统计今日报警数据")
        void shouldCalculateTodayStatistics() {
            // Given
            AlarmStatisticsVO vo = AlarmStatisticsVO.builder()
                    .total(25L)
                    .unprocessed(3L)
                    .processed(22L)
                    .build();

            when(alarmQueryRepository.getStatistics(any(), any(), any())).thenReturn(vo);
            when(alarmMapper.toStatisticsResponse(any())).thenReturn(new AlarmStatisticsResponse());

            // When
            var result = alarmService.getTodayStatistics();

            // Then
            assertThat(result).isNotNull();
            verify(alarmQueryRepository).getStatistics(any(), any(), any());
        }
    }

    private AlarmRecord createAlarmRecord(Long id, Integer type, Long siteId) {
        AlarmRecord record = new AlarmRecord();
        record.setId(id);
        record.setType(type);
        record.setSiteId(siteId);
        record.setChannelId(101L);
        record.setAlarmTime(LocalDateTime.now());
        record.setStatus(AlarmStatusEnum.UNPROCESSED.getCode());
        return record;
    }
}
