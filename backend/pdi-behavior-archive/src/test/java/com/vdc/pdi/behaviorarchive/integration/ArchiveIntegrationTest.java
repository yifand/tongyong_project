package com.vdc.pdi.behaviorarchive.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.behaviorarchive.domain.entity.ArchiveTimeline;
import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.entity.PdiTask;
import com.vdc.pdi.behaviorarchive.domain.repository.ArchiveTimelineRepository;
import com.vdc.pdi.behaviorarchive.domain.repository.BehaviorArchiveRepository;
import com.vdc.pdi.behaviorarchive.domain.repository.PdiTaskRepository;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveDetailResponse;
import com.vdc.pdi.behaviorarchive.dto.response.TimelineItemDTO;
import com.vdc.pdi.behaviorarchive.enums.ArchiveStatus;
import com.vdc.pdi.behaviorarchive.service.ArchiveTimelineService;
import com.vdc.pdi.behaviorarchive.service.BehaviorArchiveService;
import com.vdc.pdi.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 档案模块集成测试
 * 使用@SpringBootTest测试完整流程
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("档案模块集成测试")
class ArchiveIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BehaviorArchiveRepository archiveRepository;

    @Autowired
    private ArchiveTimelineRepository timelineRepository;

    @Autowired
    private PdiTaskRepository pdiTaskRepository;

    @Autowired
    private BehaviorArchiveService archiveService;

    @Autowired
    private ArchiveTimelineService timelineService;

    @Autowired
    private ObjectMapper objectMapper;

    private PdiTask createPdiTask(Long channelId, Long siteId, LocalDateTime startTime, LocalDateTime endTime,
                                  Integer estimatedDuration, Integer actualDuration, Integer result) {
        PdiTask task = new PdiTask();
        task.setChannelId(channelId);
        task.setSiteId(siteId);
        task.setStartTime(startTime);
        task.setEndTime(endTime);
        task.setEstimatedDuration(estimatedDuration);
        task.setActualDuration(actualDuration);
        task.setResult(result);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    private BehaviorArchive createArchive(Long pdiTaskId, Long channelId, Long siteId,
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          Integer estimatedDuration, Integer actualDuration,
                                          Integer status) {
        BehaviorArchive archive = new BehaviorArchive();
        archive.setPdiTaskId(pdiTaskId);
        archive.setChannelId(channelId);
        archive.setSiteId(siteId);
        archive.setStartTime(startTime);
        archive.setEndTime(endTime);
        archive.setEstimatedDuration(estimatedDuration);
        archive.setActualDuration(actualDuration);
        archive.setStatus(status);
        return archive;
    }

    private ArchiveTimeline createTimelineNode(Long archiveId, Integer seq,
                                                LocalDateTime eventTime, String action, String imageUrl) {
        ArchiveTimeline node = new ArchiveTimeline();
        node.setArchiveId(archiveId);
        node.setSeq(seq);
        node.setEventTime(eventTime);
        node.setAction(action);
        node.setImageUrl(imageUrl);
        return node;
    }

    @BeforeEach
    void setUp() {
        // 清理数据
        timelineRepository.deleteAll();
        archiveRepository.deleteAll();
        pdiTaskRepository.deleteAll();
    }

    // ==================== 档案创建 → 查询 → 更新流程测试 ====================

    @Test
    @DisplayName("档案完整流程 - 创建 → 查询 → 更新")
    void archiveFullFlow_CreateQueryUpdate() {
        // Given - 创建PDI任务
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        PdiTask pdiTask = createPdiTask(101L, 1L, startTime, null, 10, null, 0);
        PdiTask savedTask = pdiTaskRepository.save(pdiTask);

        // When - 创建档案
        BehaviorArchive archive = createArchive(
                savedTask.getId(), 101L, 1L,
                startTime, null, 10, null, ArchiveStatus.IN_PROGRESS.getCode()
        );
        BehaviorArchive savedArchive = archiveService.createOrUpdateArchive(archive);

        // Then - 验证创建
        assertThat(savedArchive.getId()).isNotNull();
        assertThat(savedArchive.getStatus()).isEqualTo(ArchiveStatus.IN_PROGRESS.getCode());

        // When - 查询档案
        ArchiveDetailResponse detail = archiveService.getArchiveDetail(savedArchive.getId());

        // Then - 验证查询
        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(savedArchive.getId());
        assertThat(detail.getStatus()).isEqualTo(ArchiveStatus.IN_PROGRESS.getCode());

        // When - 更新档案（完成）
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 25, 10, 9, 10);
        savedTask.setEndTime(endTime);
        savedTask.setActualDuration(550);
        savedTask.setResult(1);
        pdiTaskRepository.save(savedTask);

        savedArchive.setEndTime(endTime);
        savedArchive.setActualDuration(550);
        BehaviorArchive updatedArchive = archiveService.createOrUpdateArchive(savedArchive);

        // Then - 验证更新和状态计算
        assertThat(updatedArchive.getEndTime()).isEqualTo(endTime);
        assertThat(updatedArchive.getActualDuration()).isEqualTo(550);
        // 550秒 >= 600 * 0.9 = 540秒，应该达标
        assertThat(updatedArchive.getStatus()).isEqualTo(ArchiveStatus.QUALIFIED.getCode());
    }

    @Test
    @DisplayName("档案完整流程 - 创建 → 查询 → 更新（未达标场景）")
    void archiveFullFlow_Unqualified() {
        // Given - 创建PDI任务
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        PdiTask pdiTask = createPdiTask(101L, 1L, startTime, null, 10, null, 0);
        PdiTask savedTask = pdiTaskRepository.save(pdiTask);

        // When - 创建档案
        BehaviorArchive archive = createArchive(
                savedTask.getId(), 101L, 1L,
                startTime, null, 10, null, ArchiveStatus.IN_PROGRESS.getCode()
        );
        BehaviorArchive savedArchive = archiveService.createOrUpdateArchive(archive);

        // When - 更新档案（完成，但时长不足）
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 25, 10, 8, 0); // 只有480秒
        savedArchive.setEndTime(endTime);
        savedArchive.setActualDuration(480);
        BehaviorArchive updatedArchive = archiveService.createOrUpdateArchive(savedArchive);

        // Then - 验证未达标
        // 480秒 < 600 * 0.9 = 540秒，应该未达标
        assertThat(updatedArchive.getStatus()).isEqualTo(ArchiveStatus.UNQUALIFIED.getCode());
    }

    // ==================== 时间线组装完整流程测试 ====================

    @Test
    @DisplayName("时间线组装完整流程 - 已完成作业")
    void timelineAssembly_FullFlow_CompletedTask() {
        // Given - 创建PDI任务和档案
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 25, 10, 8, 32);

        PdiTask pdiTask = createPdiTask(101L, 1L, startTime, endTime, 10, 512, 1);
        PdiTask savedTask = pdiTaskRepository.save(pdiTask);

        BehaviorArchive archive = createArchive(
                savedTask.getId(), 101L, 1L,
                startTime, endTime, 10, 512, ArchiveStatus.QUALIFIED.getCode()
        );
        BehaviorArchive savedArchive = archiveRepository.save(archive);

        // 创建中间时间线节点
        ArchiveTimeline node1 = createTimelineNode(
                savedArchive.getId(), 2,
                startTime.plusMinutes(2).plusSeconds(15),
                "开始检查作业", "http://minio/check1.jpg"
        );
        ArchiveTimeline node2 = createTimelineNode(
                savedArchive.getId(), 3,
                startTime.plusMinutes(5).plusSeconds(30),
                "检查进行中", "http://minio/check2.jpg"
        );
        timelineRepository.save(node1);
        timelineRepository.save(node2);

        // When - 组装时间线
        List<TimelineItemDTO> timeline = timelineService.assembleTimeline(savedArchive, savedTask);

        // Then - 验证时间线
        assertThat(timeline).hasSize(4); // start + 2个中间节点 + end

        // 验证开始节点
        assertThat(timeline.get(0).getNodeType()).isEqualTo("start");
        assertThat(timeline.get(0).getSeq()).isEqualTo(1);
        assertThat(timeline.get(0).getOffsetSeconds()).isEqualTo(0);
        assertThat(timeline.get(0).getAction()).isEqualTo("人员进入车内");

        // 验证中间节点1
        assertThat(timeline.get(1).getNodeType()).isEqualTo("process");
        assertThat(timeline.get(1).getSeq()).isEqualTo(2);
        assertThat(timeline.get(1).getAction()).isEqualTo("开始检查作业");
        assertThat(timeline.get(1).getOffsetSeconds()).isEqualTo(135); // 2分15秒

        // 验证中间节点2
        assertThat(timeline.get(2).getNodeType()).isEqualTo("process");
        assertThat(timeline.get(2).getSeq()).isEqualTo(3);
        assertThat(timeline.get(2).getAction()).isEqualTo("检查进行中");
        assertThat(timeline.get(2).getOffsetSeconds()).isEqualTo(330); // 5分30秒

        // 验证结束节点
        assertThat(timeline.get(3).getNodeType()).isEqualTo("end");
        assertThat(timeline.get(3).getSeq()).isEqualTo(4);
        assertThat(timeline.get(3).getOffsetSeconds()).isEqualTo(512); // 8分32秒
        assertThat(timeline.get(3).getAction()).isEqualTo("人员离开，检查结束");
    }

    @Test
    @DisplayName("时间线组装完整流程 - 进行中作业")
    void timelineAssembly_FullFlow_InProgressTask() {
        // Given - 创建进行中的PDI任务和档案
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);

        PdiTask pdiTask = createPdiTask(101L, 1L, startTime, null, 10, null, 0);
        PdiTask savedTask = pdiTaskRepository.save(pdiTask);

        BehaviorArchive archive = createArchive(
                savedTask.getId(), 101L, 1L,
                startTime, null, 10, null, ArchiveStatus.IN_PROGRESS.getCode()
        );
        BehaviorArchive savedArchive = archiveRepository.save(archive);

        // 创建中间时间线节点
        ArchiveTimeline node = createTimelineNode(
                savedArchive.getId(), 2,
                startTime.plusMinutes(3),
                "检查进行中", "http://minio/process.jpg"
        );
        timelineRepository.save(node);

        // When - 组装时间线
        List<TimelineItemDTO> timeline = timelineService.assembleTimeline(savedArchive, savedTask);

        // Then - 验证时间线（没有end节点）
        assertThat(timeline).hasSize(2); // start + 1个中间节点

        assertThat(timeline.get(0).getNodeType()).isEqualTo("start");
        assertThat(timeline.get(1).getNodeType()).isEqualTo("process");

        // 确保没有end节点
        boolean hasEndNode = timeline.stream()
                .anyMatch(item -> "end".equals(item.getNodeType()));
        assertThat(hasEndNode).isFalse();
    }

    @Test
    @DisplayName("时间线组装完整流程 - 只有开始和结束")
    void timelineAssembly_FullFlow_StartAndEndOnly() {
        // Given - 创建PDI任务和档案（无中间节点）
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 25, 10, 5, 0);

        PdiTask pdiTask = createPdiTask(101L, 1L, startTime, endTime, 10, 300, 1);
        PdiTask savedTask = pdiTaskRepository.save(pdiTask);

        BehaviorArchive archive = createArchive(
                savedTask.getId(), 101L, 1L,
                startTime, endTime, 10, 300, ArchiveStatus.UNQUALIFIED.getCode()
        );
        BehaviorArchive savedArchive = archiveRepository.save(archive);

        // When - 组装时间线
        List<TimelineItemDTO> timeline = timelineService.assembleTimeline(savedArchive, savedTask);

        // Then - 验证时间线
        assertThat(timeline).hasSize(2); // start + end

        assertThat(timeline.get(0).getNodeType()).isEqualTo("start");
        assertThat(timeline.get(0).getOffsetSeconds()).isEqualTo(0);

        assertThat(timeline.get(1).getNodeType()).isEqualTo("end");
        assertThat(timeline.get(1).getOffsetSeconds()).isEqualTo(300); // 5分钟 = 300秒
    }

    // ==================== API端点集成测试 ====================

    @Test
    @DisplayName("API集成测试 - 档案列表查询")
    void apiIntegration_ListArchives() {
        // Given - 准备测试数据
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);

        for (int i = 0; i < 5; i++) {
            PdiTask task = createPdiTask(101L + i, 1L,
                    startTime.plusMinutes(i), null, 10, null, 0);
            PdiTask savedTask = pdiTaskRepository.save(task);

            BehaviorArchive archive = createArchive(
                    savedTask.getId(), 101L + i, 1L,
                    startTime.plusMinutes(i), null, 10, null, 0
            );
            archiveRepository.save(archive);
        }

        // When - 调用API
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/archives?page=1&size=10", ApiResponse.class);

        // Then - 验证响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("API集成测试 - 档案详情查询")
    void apiIntegration_GetArchiveDetail() {
        // Given - 准备测试数据
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 25, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 25, 10, 8, 32);

        PdiTask task = createPdiTask(101L, 1L, startTime, endTime, 10, 512, 1);
        PdiTask savedTask = pdiTaskRepository.save(task);

        BehaviorArchive archive = createArchive(
                savedTask.getId(), 101L, 1L,
                startTime, endTime, 10, 512, 1
        );
        BehaviorArchive savedArchive = archiveRepository.save(archive);

        // When - 调用API
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/archives/" + savedArchive.getId(), ApiResponse.class);

        // Then - 验证响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("API集成测试 - 档案不存在")
    void apiIntegration_ArchiveNotFound() {
        // When - 调用API查询不存在的档案
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/archives/99999", ApiResponse.class);

        // Then - 验证响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(1101);
    }

    // ==================== 状态计算集成测试 ====================

    @Test
    @DisplayName("状态计算集成测试 - 各种状态场景")
    void statusCalculation_Integration() {
        // Given - 进行中
        BehaviorArchive inProgress = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                null, 10, null, 0
        );

        // Given - 达标（实际时长 >= 90%标准时长）
        BehaviorArchive qualified = createArchive(
                2002L, 102L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 9, 10),
                10, 550, 0
        );

        // Given - 未达标（实际时长 < 90%标准时长）
        BehaviorArchive unqualified = createArchive(
                2003L, 103L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 0),
                10, 480, 0
        );

        // When & Then
        assertThat(archiveService.calculateArchiveStatus(inProgress))
                .isEqualTo(ArchiveStatus.IN_PROGRESS);

        assertThat(archiveService.calculateArchiveStatus(qualified))
                .isEqualTo(ArchiveStatus.QUALIFIED);

        assertThat(archiveService.calculateArchiveStatus(unqualified))
                .isEqualTo(ArchiveStatus.UNQUALIFIED);
    }
}
