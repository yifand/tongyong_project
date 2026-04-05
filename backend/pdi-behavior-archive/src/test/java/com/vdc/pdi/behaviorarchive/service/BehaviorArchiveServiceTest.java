package com.vdc.pdi.behaviorarchive.service;

import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.entity.PdiTask;
import com.vdc.pdi.behaviorarchive.domain.repository.BehaviorArchiveRepository;
import com.vdc.pdi.behaviorarchive.domain.repository.PdiTaskRepository;
import com.vdc.pdi.behaviorarchive.dto.request.ArchiveListRequest;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveDetailResponse;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveResponse;
import com.vdc.pdi.behaviorarchive.dto.response.TimelineItemDTO;
import com.vdc.pdi.behaviorarchive.enums.ArchiveStatus;
import com.vdc.pdi.behaviorarchive.exception.ArchiveException;
import com.vdc.pdi.behaviorarchive.mapper.ArchiveMapper;
import com.vdc.pdi.behaviorarchive.service.impl.BehaviorArchiveServiceImpl;
import com.vdc.pdi.common.dto.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 行为档案服务测试
 * 使用Mockito进行Service层单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("行为档案服务测试")
class BehaviorArchiveServiceTest {

    @Mock
    private BehaviorArchiveRepository archiveRepository;

    @Mock
    private PdiTaskRepository pdiTaskRepository;

    @Mock
    private ArchiveTimelineService timelineService;

    @Mock
    private ArchiveMapper archiveMapper;

    @InjectMocks
    private BehaviorArchiveServiceImpl archiveService;

    private BehaviorArchive createArchive(Long id, Integer status, LocalDateTime endTime,
                                          Integer estimatedDuration, Integer actualDuration) {
        BehaviorArchive archive = new BehaviorArchive();
        archive.setId(id);
        archive.setPdiTaskId(id + 1000);
        archive.setChannelId(101L);
        archive.setSiteId(1L);
        archive.setStartTime(LocalDateTime.of(2026, 3, 25, 10, 0, 0));
        archive.setEndTime(endTime);
        archive.setEstimatedDuration(estimatedDuration);
        archive.setActualDuration(actualDuration);
        archive.setStatus(status);
        return archive;
    }

    private PdiTask createPdiTask(Long id, LocalDateTime endTime) {
        PdiTask task = new PdiTask();
        task.setId(id);
        task.setChannelId(101L);
        task.setSiteId(1L);
        task.setStartTime(LocalDateTime.of(2026, 3, 25, 10, 0, 0));
        task.setEndTime(endTime);
        task.setEstimatedDuration(10);
        task.setActualDuration(endTime != null ? 512 : null);
        task.setResult(endTime != null ? 1 : 0);
        return task;
    }

    // ==================== calculateArchiveStatus() 测试 ====================

    @Test
    @DisplayName("计算档案状态 - 进行中状态")
    void calculateArchiveStatus_InProgress() {
        // Given
        BehaviorArchive archive = createArchive(1001L, 0, null, 10, null);

        // When
        ArchiveStatus status = archiveService.calculateArchiveStatus(archive);

        // Then
        assertThat(status).isEqualTo(ArchiveStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("计算档案状态 - 达标（实际时长 >= 标准时长 * 0.9）")
    void calculateArchiveStatus_Qualified() {
        // Given
        // 标准时长10分钟 = 600秒，90%阈值 = 540秒
        // 实际时长550秒 >= 540秒，应判定为达标
        BehaviorArchive archive = createArchive(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 9, 10), 10, 550);

        // When
        ArchiveStatus status = archiveService.calculateArchiveStatus(archive);

        // Then
        assertThat(status).isEqualTo(ArchiveStatus.QUALIFIED);
    }

    @Test
    @DisplayName("计算档案状态 - 达标边界值（实际时长 == 标准时长 * 0.9）")
    void calculateArchiveStatus_Qualified_Boundary() {
        // Given
        // 标准时长10分钟 = 600秒，90%阈值 = 540秒
        // 实际时长540秒 == 540秒，应判定为达标
        BehaviorArchive archive = createArchive(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 9, 0), 10, 540);

        // When
        ArchiveStatus status = archiveService.calculateArchiveStatus(archive);

        // Then
        assertThat(status).isEqualTo(ArchiveStatus.QUALIFIED);
    }

    @Test
    @DisplayName("计算档案状态 - 未达标（实际时长 < 标准时长 * 0.9）")
    void calculateArchiveStatus_Unqualified() {
        // Given
        // 标准时长10分钟 = 600秒，90%阈值 = 540秒
        // 实际时长500秒 < 540秒，应判定为未达标
        BehaviorArchive archive = createArchive(1001L, 2,
                LocalDateTime.of(2026, 3, 25, 10, 8, 20), 10, 500);

        // When
        ArchiveStatus status = archiveService.calculateArchiveStatus(archive);

        // Then
        assertThat(status).isEqualTo(ArchiveStatus.UNQUALIFIED);
    }

    @Test
    @DisplayName("计算档案状态 - 未达标（无预估时长）")
    void calculateArchiveStatus_Unqualified_NoEstimatedDuration() {
        // Given
        BehaviorArchive archive = createArchive(1001L, 2,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32), null, 512);

        // When
        ArchiveStatus status = archiveService.calculateArchiveStatus(archive);

        // Then
        assertThat(status).isEqualTo(ArchiveStatus.UNQUALIFIED);
    }

    @Test
    @DisplayName("计算档案状态 - 未达标（预估时长为0）")
    void calculateArchiveStatus_Unqualified_ZeroEstimatedDuration() {
        // Given
        BehaviorArchive archive = createArchive(1001L, 2,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32), 0, 512);

        // When
        ArchiveStatus status = archiveService.calculateArchiveStatus(archive);

        // Then
        assertThat(status).isEqualTo(ArchiveStatus.UNQUALIFIED);
    }

    // ==================== queryArchiveList() 测试 ====================

    @Test
    @DisplayName("查询档案列表 - 正常查询")
    void queryArchiveList_Success() {
        // Given
        ArchiveListRequest request = new ArchiveListRequest();
        request.setPage(1);
        request.setSize(20);

        BehaviorArchive archive1 = createArchive(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32), 10, 512);
        BehaviorArchive archive2 = createArchive(1002L, 2,
                LocalDateTime.of(2026, 3, 25, 9, 35, 20), 15, 305);

        Page<BehaviorArchive> pageResult = new PageImpl<>(
                List.of(archive1, archive2),
                PageRequest.of(0, 20),
                2
        );

        when(archiveRepository.findArchivesWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(pageResult);

        ArchiveResponse response1 = new ArchiveResponse();
        response1.setId(1001L);
        ArchiveResponse response2 = new ArchiveResponse();
        response2.setId(1002L);

        when(archiveMapper.toResponseList(anyList()))
                .thenReturn(List.of(response1, response2));

        // When
        PageResult<ArchiveResponse> result = archiveService.queryArchiveList(request, 1, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(2);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("查询档案列表 - 带筛选条件查询")
    void queryArchiveList_WithFilters() {
        // Given
        ArchiveListRequest request = new ArchiveListRequest();
        request.setSiteId(1L);
        request.setStatus(1);
        request.setChannelId(101L);
        request.setStartTimeFrom(LocalDateTime.of(2026, 3, 1, 0, 0, 0));
        request.setStartTimeTo(LocalDateTime.of(2026, 3, 31, 23, 59, 59));
        request.setPage(1);
        request.setSize(20);

        BehaviorArchive archive = createArchive(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32), 10, 512);

        Page<BehaviorArchive> pageResult = new PageImpl<>(
                List.of(archive),
                PageRequest.of(0, 20),
                1
        );

        when(archiveRepository.findArchivesWithFilters(
                eq(1L), eq(1), eq(101L), any(), any(), any(Pageable.class)))
                .thenReturn(pageResult);

        ArchiveResponse response = new ArchiveResponse();
        response.setId(1001L);

        when(archiveMapper.toResponseList(anyList()))
                .thenReturn(List.of(response));

        // When
        PageResult<ArchiveResponse> result = archiveService.queryArchiveList(request, 1, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        verify(archiveRepository).findArchivesWithFilters(
                eq(1L), eq(1), eq(101L), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("查询档案列表 - 空结果")
    void queryArchiveList_EmptyResult() {
        // Given
        ArchiveListRequest request = new ArchiveListRequest();
        request.setPage(1);
        request.setSize(20);

        Page<BehaviorArchive> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(archiveRepository.findArchivesWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        when(archiveMapper.toResponseList(anyList()))
                .thenReturn(List.of());

        // When
        PageResult<ArchiveResponse> result = archiveService.queryArchiveList(request, 1, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0);
    }

    // ==================== getArchiveDetail() 测试 ====================

    @Test
    @DisplayName("获取档案详情 - 成功")
    void getArchiveDetail_Success() {
        // Given
        Long archiveId = 1001L;
        BehaviorArchive archive = createArchive(archiveId, 1,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32), 10, 512);
        PdiTask pdiTask = createPdiTask(2001L, LocalDateTime.of(2026, 3, 25, 10, 8, 32));

        ArchiveDetailResponse detailResponse = new ArchiveDetailResponse();
        detailResponse.setId(archiveId);
        detailResponse.setStatus(1);

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));
        when(pdiTaskRepository.findById(archive.getPdiTaskId())).thenReturn(Optional.of(pdiTask));
        when(archiveMapper.toDetailResponse(archive)).thenReturn(detailResponse);
        when(timelineService.assembleTimeline(archive, pdiTask))
                .thenReturn(List.of());

        // When
        ArchiveDetailResponse result = archiveService.getArchiveDetail(archiveId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(archiveId);
        verify(timelineService).assembleTimeline(archive, pdiTask);
    }

    @Test
    @DisplayName("获取档案详情 - 档案不存在")
    void getArchiveDetail_NotFound() {
        // Given
        Long archiveId = 999L;
        when(archiveRepository.findById(archiveId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> archiveService.getArchiveDetail(archiveId))
                .isInstanceOf(ArchiveException.class)
                .hasMessageContaining("档案不存在");
    }

    @Test
    @DisplayName("获取档案详情 - 关联的PDI任务不存在")
    void getArchiveDetail_PdiTaskNotFound() {
        // Given
        Long archiveId = 1001L;
        BehaviorArchive archive = createArchive(archiveId, 1,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32), 10, 512);

        when(archiveRepository.findById(archiveId)).thenReturn(Optional.of(archive));
        when(pdiTaskRepository.findById(archive.getPdiTaskId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> archiveService.getArchiveDetail(archiveId))
                .isInstanceOf(ArchiveException.class)
                .hasMessageContaining("关联的PDI任务不存在");
    }

    // ==================== createOrUpdateArchive() 测试 ====================

    @Test
    @DisplayName("创建或更新档案 - 创建新档案")
    void createOrUpdateArchive_CreateNew() {
        // Given
        BehaviorArchive archive = new BehaviorArchive();
        archive.setPdiTaskId(2001L);
        archive.setChannelId(101L);
        archive.setSiteId(1L);
        archive.setStartTime(LocalDateTime.of(2026, 3, 25, 10, 0, 0));

        BehaviorArchive savedArchive = new BehaviorArchive();
        savedArchive.setId(1001L);
        savedArchive.setPdiTaskId(2001L);
        savedArchive.setStatus(ArchiveStatus.IN_PROGRESS.getCode());

        when(archiveRepository.save(any(BehaviorArchive.class))).thenReturn(savedArchive);

        // When
        BehaviorArchive result = archiveService.createOrUpdateArchive(archive);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getStatus()).isEqualTo(ArchiveStatus.IN_PROGRESS.getCode());
        verify(archiveRepository).save(archive);
    }

    @Test
    @DisplayName("创建或更新档案 - 更新已有档案")
    void createOrUpdateArchive_UpdateExisting() {
        // Given
        BehaviorArchive archive = createArchive(1001L, 0, null, 10, null);
        archive.setEndTime(LocalDateTime.of(2026, 3, 25, 10, 9, 10));
        archive.setActualDuration(550);

        BehaviorArchive savedArchive = createArchive(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 9, 10), 10, 550);
        savedArchive.setStatus(ArchiveStatus.QUALIFIED.getCode());

        when(archiveRepository.save(any(BehaviorArchive.class))).thenReturn(savedArchive);

        // When
        BehaviorArchive result = archiveService.createOrUpdateArchive(archive);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ArchiveStatus.QUALIFIED.getCode());
        verify(archiveRepository).save(archive);
    }
}
