package com.vdc.pdi.behaviorarchive.repository;

import com.vdc.pdi.behaviorarchive.domain.entity.ArchiveTimeline;
import com.vdc.pdi.behaviorarchive.domain.repository.ArchiveTimelineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 档案时间线Repository测试
 * 使用@DataJpaTest进行Repository层单元测试
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("档案时间线Repository测试")
class ArchiveTimelineRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ArchiveTimelineRepository timelineRepository;

    private ArchiveTimeline createTimelineNode(Long archiveId, Integer seq,
                                                LocalDateTime eventTime, String action, String imageUrl) {
        ArchiveTimeline node = new ArchiveTimeline();
        node.setArchiveId(archiveId);
        node.setSeq(seq);
        node.setEventTime(eventTime);
        node.setAction(action);
        node.setImageUrl(imageUrl);
        node.setCreatedAt(LocalDateTime.now());
        return node;
    }

    // ==================== 根据archiveId查询测试 ====================

    @Test
    @DisplayName("根据档案ID查询 - 存在多个节点")
    void findByArchiveId_Exists() {
        // Given
        Long archiveId = 1001L;
        ArchiveTimeline node1 = createTimelineNode(archiveId, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "人员进入车内", "http://minio/enter.jpg");
        ArchiveTimeline node2 = createTimelineNode(archiveId, 2,
                LocalDateTime.of(2026, 3, 25, 10, 5, 0),
                "检查进行中", "http://minio/process.jpg");
        ArchiveTimeline node3 = createTimelineNode(archiveId, 3,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                "人员离开，检查结束", "http://minio/exit.jpg");

        entityManager.persist(node1);
        entityManager.persist(node2);
        entityManager.persist(node3);
        entityManager.flush();

        // When
        List<ArchiveTimeline> result = timelineRepository.findByArchiveId(archiveId);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("根据档案ID查询 - 不存在")
    void findByArchiveId_NotExists() {
        // When
        List<ArchiveTimeline> result = timelineRepository.findByArchiveId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("根据档案ID查询 - 多个档案数据隔离")
    void findByArchiveId_DataIsolation() {
        // Given
        ArchiveTimeline node1 = createTimelineNode(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "进入", "http://minio/1.jpg");
        ArchiveTimeline node2 = createTimelineNode(1002L, 1,
                LocalDateTime.of(2026, 3, 25, 9, 30, 0),
                "进入", "http://minio/2.jpg");
        ArchiveTimeline node3 = createTimelineNode(1002L, 2,
                LocalDateTime.of(2026, 3, 25, 9, 35, 0),
                "离开", "http://minio/3.jpg");

        entityManager.persist(node1);
        entityManager.persist(node2);
        entityManager.persist(node3);
        entityManager.flush();

        // When
        List<ArchiveTimeline> result1 = timelineRepository.findByArchiveId(1001L);
        List<ArchiveTimeline> result2 = timelineRepository.findByArchiveId(1002L);

        // Then
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(2);
    }

    // ==================== 根据archiveId和seq排序查询测试 ====================

    @Test
    @DisplayName("根据档案ID和seq排序查询 - 升序")
    void findByArchiveIdOrderBySeqAsc() {
        // Given
        Long archiveId = 1001L;
        ArchiveTimeline node1 = createTimelineNode(archiveId, 3,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                "离开", "http://minio/exit.jpg");
        ArchiveTimeline node2 = createTimelineNode(archiveId, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "进入", "http://minio/enter.jpg");
        ArchiveTimeline node3 = createTimelineNode(archiveId, 2,
                LocalDateTime.of(2026, 3, 25, 10, 5, 0),
                "检查", "http://minio/check.jpg");

        entityManager.persist(node1);
        entityManager.persist(node2);
        entityManager.persist(node3);
        entityManager.flush();

        // When
        List<ArchiveTimeline> result = timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSeq()).isEqualTo(1);
        assertThat(result.get(1).getSeq()).isEqualTo(2);
        assertThat(result.get(2).getSeq()).isEqualTo(3);
        assertThat(result.get(0).getAction()).isEqualTo("进入");
        assertThat(result.get(1).getAction()).isEqualTo("检查");
        assertThat(result.get(2).getAction()).isEqualTo("离开");
    }

    @Test
    @DisplayName("根据档案ID和seq大于指定值查询 - 按seq升序")
    void findByArchiveIdAndSeqGreaterThanOrderBySeqAsc() {
        // Given
        Long archiveId = 1001L;
        ArchiveTimeline node1 = createTimelineNode(archiveId, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "进入", "http://minio/enter.jpg");
        ArchiveTimeline node2 = createTimelineNode(archiveId, 2,
                LocalDateTime.of(2026, 3, 25, 10, 2, 0),
                "开始检查", "http://minio/start.jpg");
        ArchiveTimeline node3 = createTimelineNode(archiveId, 3,
                LocalDateTime.of(2026, 3, 25, 10, 5, 0),
                "检查中", "http://minio/process.jpg");
        ArchiveTimeline node4 = createTimelineNode(archiveId, 4,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                "离开", "http://minio/exit.jpg");

        entityManager.persist(node1);
        entityManager.persist(node2);
        entityManager.persist(node3);
        entityManager.persist(node4);
        entityManager.flush();

        // When - 查询seq大于1的节点（排除开始节点）
        List<ArchiveTimeline> result = timelineRepository
                .findByArchiveIdAndSeqGreaterThanOrderBySeqAsc(archiveId, 1);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSeq()).isEqualTo(2);
        assertThat(result.get(1).getSeq()).isEqualTo(3);
        assertThat(result.get(2).getSeq()).isEqualTo(4);
    }

    @Test
    @DisplayName("根据档案ID和seq大于指定值查询 - 无结果")
    void findByArchiveIdAndSeqGreaterThanOrderBySeqAsc_Empty() {
        // Given
        Long archiveId = 1001L;
        ArchiveTimeline node1 = createTimelineNode(archiveId, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "进入", "http://minio/enter.jpg");

        entityManager.persist(node1);
        entityManager.flush();

        // When - 查询seq大于10的节点
        List<ArchiveTimeline> result = timelineRepository
                .findByArchiveIdAndSeqGreaterThanOrderBySeqAsc(archiveId, 10);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== 查询最大seq测试 ====================

    @Test
    @DisplayName("查询最大seq - 存在节点")
    void findMaxSeqByArchiveId_Exists() {
        // Given
        Long archiveId = 1001L;
        ArchiveTimeline node1 = createTimelineNode(archiveId, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "进入", "http://minio/enter.jpg");
        ArchiveTimeline node2 = createTimelineNode(archiveId, 5,
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                "离开", "http://minio/exit.jpg");

        entityManager.persist(node1);
        entityManager.persist(node2);
        entityManager.flush();

        // When
        Optional<Integer> maxSeq = timelineRepository.findMaxSeqByArchiveId(archiveId);

        // Then
        assertThat(maxSeq).isPresent();
        assertThat(maxSeq.get()).isEqualTo(5);
    }

    @Test
    @DisplayName("查询最大seq - 不存在")
    void findMaxSeqByArchiveId_NotExists() {
        // When
        Optional<Integer> maxSeq = timelineRepository.findMaxSeqByArchiveId(999L);

        // Then
        assertThat(maxSeq).isEmpty();
    }

    // ==================== 保存和更新测试 ====================

    @Test
    @DisplayName("保存时间线节点 - 正常保存")
    void save_Success() {
        // Given
        ArchiveTimeline node = createTimelineNode(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "人员进入车内", "http://minio/enter.jpg");

        // When
        ArchiveTimeline saved = timelineRepository.save(node);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getArchiveId()).isEqualTo(1001L);
        assertThat(saved.getSeq()).isEqualTo(1);
    }

    @Test
    @DisplayName("更新时间线节点 - 正常更新")
    void update_Success() {
        // Given
        ArchiveTimeline node = createTimelineNode(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "进入", "http://minio/enter.jpg");
        entityManager.persist(node);
        entityManager.flush();

        // When
        node.setAction("人员进入车内");
        node.setImageUrl("http://minio/new_enter.jpg");
        ArchiveTimeline updated = timelineRepository.save(node);
        entityManager.flush();

        // Then
        assertThat(updated.getAction()).isEqualTo("人员进入车内");
        assertThat(updated.getImageUrl()).isEqualTo("http://minio/new_enter.jpg");
    }

    // ==================== 逻辑删除测试 ====================

    @Test
    @DisplayName("逻辑删除 - 节点被标记为删除")
    void logicalDelete() {
        // Given
        ArchiveTimeline node = createTimelineNode(1001L, 1,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                "进入", "http://minio/enter.jpg");
        entityManager.persist(node);
        entityManager.flush();

        // When
        node.setDeletedAt(LocalDateTime.now());
        entityManager.merge(node);
        entityManager.flush();

        // Then - 查询最大seq时不应包含已删除的节点
        Optional<Integer> maxSeq = timelineRepository.findMaxSeqByArchiveId(1001L);
        assertThat(maxSeq).isEmpty();
    }
}
