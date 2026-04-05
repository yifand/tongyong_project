package com.vdc.pdi.behaviorarchive.service;

import com.vdc.pdi.behaviorarchive.domain.entity.ArchiveTimeline;
import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.entity.PdiTask;
import com.vdc.pdi.behaviorarchive.domain.entity.StateStream;
import com.vdc.pdi.behaviorarchive.domain.repository.ArchiveTimelineRepository;
import com.vdc.pdi.behaviorarchive.domain.repository.StateStreamRepository;
import com.vdc.pdi.behaviorarchive.dto.response.TimelineItemDTO;
import com.vdc.pdi.behaviorarchive.service.impl.ArchiveTimelineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 档案时间线服务测试
 * 使用Mockito进行Service层单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("档案时间线服务测试")
class ArchiveTimelineServiceTest {

    @Mock
    private ArchiveTimelineRepository timelineRepository;

    @Mock
    private StateStreamRepository stateStreamRepository;

    @InjectMocks
    private ArchiveTimelineServiceImpl timelineService;

    private BehaviorArchive createArchive(Long id) {
        BehaviorArchive archive = new BehaviorArchive();
        archive.setId(id);
        archive.setPdiTaskId(id + 1000);
        archive.setChannelId(101L);
        archive.setSiteId(1L);
        return archive;
    }

    private PdiTask createPdiTask(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        PdiTask task = new PdiTask();
        task.setId(id);
        task.setChannelId(101L);
        task.setSiteId(1L);
        task.setStartTime(startTime);
        task.setEndTime(endTime);
        task.setEstimatedDuration(10);
        if (endTime != null) {
            task.setActualDuration((int) java.time.Duration.between(startTime, endTime).getSeconds());
        }
        return task;
    }

    private ArchiveTimeline createTimelineNode(Long id, Long archiveId, Integer seq,
                                                LocalDateTime eventTime, String action, String imageUrl) {
        ArchiveTimeline node = new ArchiveTimeline();
        node.setId(id);
        node.setArchiveId(archiveId);
        node.setSeq(seq);
        node.setEventTime(eventTime);
        node.setAction(action);
        node.setImageUrl(imageUrl);
        return node;
    }

    // ==================== assembleTimeline() 测试 ====================

    @Test
    @DisplayName("组装时间线 - 已完成作业（有开始和结束节点）")
    void assembleTimeline_CompletedTask() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 25, 10, 8, 32);

        BehaviorArchive archive = createArchive(1L);
        PdiTask pdiTask = createPdiTask(2001L, startTime, endTime);

        when(timelineRepository.findByArchiveIdAndSeqGreaterThanOrderBySeqAsc(eq(1L), eq(1)))
                .thenReturn(List.of());
        when(stateStreamRepository.findFirstByChannelIdAndStateCodeAndEventTimeGreaterThanEqualOrderByEventTimeAsc(
                anyLong(), eq(3), any()))
                .thenReturn(Optional.empty());
        when(stateStreamRepository.findFirstByChannelIdAndStateCodeInAndEventTimeLessThanEqualOrderByEventTimeDesc(
                anyLong(), any(Collection.class), any()))
                .thenReturn(Optional.empty());

        // When
        List<TimelineItemDTO> timeline = timelineService.assembleTimeline(archive, pdiTask);

        // Then
        assertThat(timeline).hasSize(2);

        // 验证开始节点
        TimelineItemDTO startNode = timeline.get(0);
        assertThat(startNode.getSeq()).isEqualTo(1);
        assertThat(startNode.getNodeType()).isEqualTo("start");
        assertThat(startNode.getEventTime()).isEqualTo(startTime);
        assertThat(startNode.getAction()).isEqualTo("人员进入车内");
        assertThat(startNode.getOffsetSeconds()).isEqualTo(0);

        // 验证结束节点
        TimelineItemDTO endNode = timeline.get(1);
        assertThat(endNode.getSeq()).isEqualTo(2);
        assertThat(endNode.getNodeType()).isEqualTo("end");
        assertThat(endNode.getEventTime()).isEqualTo(endTime);
        assertThat(endNode.getAction()).isEqualTo("人员离开，检查结束");
        assertThat(endNode.getOffsetSeconds()).isEqualTo(512); // 8分32秒 = 512秒
    }

    @Test
    @DisplayName("组装时间线 - 进行中作业（只有开始节点）")
    void assembleTimeline_InProgressTask() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);

        BehaviorArchive archive = createArchive(1L);
        PdiTask pdiTask = createPdiTask(2001L, startTime, null); // 进行中，无结束时间

        // 中间节点
        ArchiveTimeline middleNode = createTimelineNode(2L, 1L, 2,
                startTime.plusMinutes(2), "检查进行中", "http://minio/check.jpg");

        when(timelineRepository.findByArchiveIdAndSeqGreaterThanOrderBySeqAsc(eq(1L), eq(1)))
                .thenReturn(List.of(middleNode));
        when(stateStreamRepository.findFirstByChannelIdAndStateCodeAndEventTimeGreaterThanEqualOrderByEventTimeAsc(
                anyLong(), eq(3), any()))
                .thenReturn(Optional.empty());

        // When
        List<TimelineItemDTO> timeline = timelineService.assembleTimeline(archive, pdiTask);

        // Then
        assertThat(timeline).hasSize(2); // 只有start和process，没有end

        // 验证开始节点
        assertThat(timeline.get(0).getNodeType()).isEqualTo("start");
        assertThat(timeline.get(0).getSeq()).isEqualTo(1);

        // 验证中间节点
        TimelineItemDTO processNode = timeline.get(1);
        assertThat(processNode.getNodeType()).isEqualTo("process");
        assertThat(processNode.getSeq()).isEqualTo(2);
        assertThat(processNode.getAction()).isEqualTo("检查进行中");
        assertThat(processNode.getOffsetSeconds()).isEqualTo(120); // 2分钟 = 120秒
    }

    @Test
    @DisplayName("组装时间线 - 包含中间时间线节点")
    void assembleTimeline_WithMiddleNodes() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 25, 10, 8, 32);

        BehaviorArchive archive = createArchive(1L);
        PdiTask pdiTask = createPdiTask(2001L, startTime, endTime);

        // 中间节点
        ArchiveTimeline node1 = createTimelineNode(2L, 1L, 2,
                startTime.plusMinutes(2).plusSeconds(15), "开始检查作业", "http://minio/check1.jpg");
        ArchiveTimeline node2 = createTimelineNode(3L, 1L, 3,
                startTime.plusMinutes(5).plusSeconds(30), "检查进行中", "http://minio/check2.jpg");

        when(timelineRepository.findByArchiveIdAndSeqGreaterThanOrderBySeqAsc(eq(1L), eq(1)))
                .thenReturn(List.of(node1, node2));
        when(stateStreamRepository.findFirstByChannelIdAndStateCodeAndEventTimeGreaterThanEqualOrderByEventTimeAsc(
                anyLong(), eq(3), any()))
                .thenReturn(Optional.empty());
        when(stateStreamRepository.findFirstByChannelIdAndStateCodeInAndEventTimeLessThanEqualOrderByEventTimeDesc(
                anyLong(), any(Collection.class), any()))
                .thenReturn(Optional.empty());

        // When
        List<TimelineItemDTO> timeline = timelineService.assembleTimeline(archive, pdiTask);

        // Then
        assertThat(timeline).hasSize(4); // start + 2个中间节点 + end

        // 验证所有节点
        assertThat(timeline.get(0).getNodeType()).isEqualTo("start");
        assertThat(timeline.get(0).getOffsetSeconds()).isEqualTo(0);

        assertThat(timeline.get(1).getNodeType()).isEqualTo("process");
        assertThat(timeline.get(1).getAction()).isEqualTo("开始检查作业");
        assertThat(timeline.get(1).getOffsetSeconds()).isEqualTo(135); // 2分15秒

        assertThat(timeline.get(2).getNodeType()).isEqualTo("process");
        assertThat(timeline.get(2).getAction()).isEqualTo("检查进行中");
        assertThat(timeline.get(2).getOffsetSeconds()).isEqualTo(330); // 5分30秒

        assertThat(timeline.get(3).getNodeType()).isEqualTo("end");
        assertThat(timeline.get(3).getOffsetSeconds()).isEqualTo(512); // 8分32秒
    }

    @Test
    @DisplayName("组装时间线 - 包含截图URL")
    void assembleTimeline_WithImageUrls() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 25, 10, 8, 32);

        BehaviorArchive archive = createArchive(1L);
        PdiTask pdiTask = createPdiTask(2001L, startTime, endTime);

        // 模拟state_stream中的截图
        StateStream enterState = new StateStream();
        enterState.setImageUrl("http://minio/enter.jpg");
        StateStream exitState = new StateStream();
        exitState.setImageUrl("http://minio/exit.jpg");

        when(timelineRepository.findByArchiveIdAndSeqGreaterThanOrderBySeqAsc(eq(1L), eq(1)))
                .thenReturn(List.of());
        when(stateStreamRepository.findFirstByChannelIdAndStateCodeAndEventTimeGreaterThanEqualOrderByEventTimeAsc(
                anyLong(), eq(3), any()))
                .thenReturn(Optional.of(enterState));
        when(stateStreamRepository.findFirstByChannelIdAndStateCodeInAndEventTimeLessThanEqualOrderByEventTimeDesc(
                anyLong(), any(Collection.class), any()))
                .thenReturn(Optional.of(exitState));

        // When
        List<TimelineItemDTO> timeline = timelineService.assembleTimeline(archive, pdiTask);

        // Then
        assertThat(timeline).hasSize(2);
        assertThat(timeline.get(0).getImageUrl()).isEqualTo("http://minio/enter.jpg");
        assertThat(timeline.get(1).getImageUrl()).isEqualTo("http://minio/exit.jpg");
    }

    @Test
    @DisplayName("组装时间线 - PDI任务开始时间为空")
    void assembleTimeline_NullStartTime() {
        // Given
        BehaviorArchive archive = createArchive(1L);
        PdiTask pdiTask = createPdiTask(2001L, null, null);

        // When
        List<TimelineItemDTO> timeline = timelineService.assembleTimeline(archive, pdiTask);

        // Then
        assertThat(timeline).isEmpty();
    }

    // ==================== getTimelineByArchiveId() 测试 ====================

    @Test
    @DisplayName("根据档案ID获取时间线 - 正常流程")
    void getTimelineByArchiveId_Success() {
        // Given
        Long archiveId = 1L;
        ArchiveTimeline node1 = createTimelineNode(1L, archiveId, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0), "进入", "http://minio/1.jpg");
        ArchiveTimeline node2 = createTimelineNode(2L, archiveId, 2,
                LocalDateTime.of(2026, 3, 25, 10, 5, 0), "检查", "http://minio/2.jpg");

        when(timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId))
                .thenReturn(List.of(node1, node2));

        // When
        List<TimelineItemDTO> timeline = timelineService.getTimelineByArchiveId(archiveId);

        // Then
        assertThat(timeline).hasSize(2);
        assertThat(timeline.get(0).getSeq()).isEqualTo(1);
        assertThat(timeline.get(0).getAction()).isEqualTo("进入");
        assertThat(timeline.get(1).getSeq()).isEqualTo(2);
        assertThat(timeline.get(1).getAction()).isEqualTo("检查");
    }

    @Test
    @DisplayName("根据档案ID获取时间线 - 空结果")
    void getTimelineByArchiveId_EmptyResult() {
        // Given
        Long archiveId = 1L;
        when(timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId))
                .thenReturn(List.of());

        // When
        List<TimelineItemDTO> timeline = timelineService.getTimelineByArchiveId(archiveId);

        // Then
        assertThat(timeline).isEmpty();
    }
}
