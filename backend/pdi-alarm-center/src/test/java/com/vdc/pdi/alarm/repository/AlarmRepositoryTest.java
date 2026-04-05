package com.vdc.pdi.alarm.repository;

import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import com.vdc.pdi.alarm.domain.repository.AlarmRepository;
import com.vdc.pdi.common.enums.AlarmStatusEnum;
import com.vdc.pdi.common.enums.AlarmTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * 报警Repository测试
 */
@DataJpaTest
@ActiveProfiles("test")
class AlarmRepositoryTest {

    @Autowired
    private AlarmRepository alarmRepository;

    @Test
    @DisplayName("应保存报警记录")
    void shouldSaveAlarmRecord() {
        // Given
        AlarmRecord alarm = new AlarmRecord();
        alarm.setType(AlarmTypeEnum.SMOKE.getCode());
        alarm.setSiteId(1L);
        alarm.setChannelId(101L);
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setStatus(AlarmStatusEnum.UNPROCESSED.getCode());
        alarm.setLocation("测试位置");

        // When
        AlarmRecord saved = alarmRepository.save(alarm);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getType()).isEqualTo(AlarmTypeEnum.SMOKE.getCode());
    }

    @Test
    @DisplayName("应按ID查询报警")
    void shouldFindAlarmById() {
        // Given
        AlarmRecord alarm = new AlarmRecord();
        alarm.setType(AlarmTypeEnum.PDI_VIOLATION.getCode());
        alarm.setSiteId(1L);
        alarm.setChannelId(102L);
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setStatus(AlarmStatusEnum.UNPROCESSED.getCode());
        AlarmRecord saved = alarmRepository.save(alarm);

        // When
        Optional<AlarmRecord> found = alarmRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("应更新报警状态")
    void shouldUpdateAlarmStatus() {
        // Given
        AlarmRecord alarm = new AlarmRecord();
        alarm.setType(AlarmTypeEnum.SMOKE.getCode());
        alarm.setSiteId(1L);
        alarm.setChannelId(101L);
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setStatus(AlarmStatusEnum.UNPROCESSED.getCode());
        AlarmRecord saved = alarmRepository.save(alarm);

        // When
        saved.setStatus(AlarmStatusEnum.PROCESSED.getCode());
        saved.setProcessedAt(LocalDateTime.now());
        saved.setProcessorId(1L);
        AlarmRecord updated = alarmRepository.save(saved);

        // Then
        assertThat(updated.getStatus()).isEqualTo(AlarmStatusEnum.PROCESSED.getCode());
        assertThat(updated.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("应逻辑删除报警")
    void shouldSoftDeleteAlarm() {
        // Given
        AlarmRecord alarm = new AlarmRecord();
        alarm.setType(AlarmTypeEnum.SMOKE.getCode());
        alarm.setSiteId(1L);
        alarm.setChannelId(101L);
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setStatus(AlarmStatusEnum.UNPROCESSED.getCode());
        AlarmRecord saved = alarmRepository.save(alarm);

        // When
        saved.setDeletedAt(LocalDateTime.now());
        alarmRepository.save(saved);

        // Then - 逻辑删除后仍可通过ID查询（实际业务中应使用查询条件过滤）
        Optional<AlarmRecord> found = alarmRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }
}
