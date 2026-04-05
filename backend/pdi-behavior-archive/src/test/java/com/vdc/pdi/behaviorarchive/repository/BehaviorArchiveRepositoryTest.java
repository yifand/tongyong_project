package com.vdc.pdi.behaviorarchive.repository;

import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.repository.BehaviorArchiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 行为档案Repository测试
 * 使用@DataJpaTest进行Repository层单元测试
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("行为档案Repository测试")
class BehaviorArchiveRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BehaviorArchiveRepository archiveRepository;

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
        archive.setCreatedAt(LocalDateTime.now());
        return archive;
    }

    @BeforeEach
    void setUp() {
        // 清理数据
        archiveRepository.findAll().forEach(archive -> {
            archive.setDeletedAt(LocalDateTime.now());
            entityManager.merge(archive);
        });
        entityManager.flush();
    }

    // ==================== 基础CRUD操作测试 ====================

    @Test
    @DisplayName("保存档案 - 正常保存")
    void save_Success() {
        // Given
        BehaviorArchive archive = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );

        // When
        BehaviorArchive saved = archiveRepository.save(archive);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPdiTaskId()).isEqualTo(2001L);
        assertThat(saved.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("根据ID查询 - 存在")
    void findById_Exists() {
        // Given
        BehaviorArchive archive = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        entityManager.persist(archive);
        entityManager.flush();

        // When
        Optional<BehaviorArchive> found = archiveRepository.findById(archive.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPdiTaskId()).isEqualTo(2001L);
    }

    @Test
    @DisplayName("根据ID查询 - 不存在")
    void findById_NotExists() {
        // When
        Optional<BehaviorArchive> found = archiveRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("更新档案 - 正常更新")
    void update_Success() {
        // Given
        BehaviorArchive archive = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                null, 10, null, 0
        );
        entityManager.persist(archive);
        entityManager.flush();

        // When
        archive.setEndTime(LocalDateTime.of(2026, 3, 25, 10, 9, 10));
        archive.setActualDuration(550);
        archive.setStatus(1);
        BehaviorArchive updated = archiveRepository.save(archive);
        entityManager.flush();

        // Then
        assertThat(updated.getStatus()).isEqualTo(1);
        assertThat(updated.getActualDuration()).isEqualTo(550);
    }

    @Test
    @DisplayName("删除档案 - 逻辑删除")
    void delete_LogicalDelete() {
        // Given
        BehaviorArchive archive = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        entityManager.persist(archive);
        entityManager.flush();

        // When
        archive.setDeletedAt(LocalDateTime.now());
        entityManager.merge(archive);
        entityManager.flush();

        // Then
        Optional<BehaviorArchive> found = archiveRepository.findById(archive.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isDeleted()).isTrue();
    }

    // ==================== 分页查询测试 ====================

    @Test
    @DisplayName("分页查询 - 正常分页")
    void findArchivesWithFilters_Pagination() {
        // Given
        for (int i = 0; i < 25; i++) {
            BehaviorArchive archive = createArchive(
                    2000L + i, 100L + i, 1L,
                    LocalDateTime.of(2026, 3, 25, 10, i),
                    LocalDateTime.of(2026, 3, 25, 10, i + 5),
                    10, 300 + i, 1
            );
            entityManager.persist(archive);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BehaviorArchive> page = archiveRepository.findArchivesWithFilters(
                null, null, null, null, null, pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("分页查询 - 第二页")
    void findArchivesWithFilters_SecondPage() {
        // Given
        for (int i = 0; i < 25; i++) {
            BehaviorArchive archive = createArchive(
                    2000L + i, 100L + i, 1L,
                    LocalDateTime.of(2026, 3, 25, 10, i),
                    LocalDateTime.of(2026, 3, 25, 10, i + 5),
                    10, 300 + i, 1
            );
            entityManager.persist(archive);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 10);

        // When
        Page<BehaviorArchive> page = archiveRepository.findArchivesWithFilters(
                null, null, null, null, null, pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    // ==================== 根据pdiTaskId查询测试 ====================

    @Test
    @DisplayName("根据PDI任务ID查询 - 存在")
    void findByPdiTaskId_Exists() {
        // Given
        BehaviorArchive archive = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        entityManager.persist(archive);
        entityManager.flush();

        // When
        Optional<BehaviorArchive> found = archiveRepository.findByPdiTaskId(2001L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPdiTaskId()).isEqualTo(2001L);
    }

    @Test
    @DisplayName("根据PDI任务ID查询 - 不存在")
    void findByPdiTaskId_NotExists() {
        // When
        Optional<BehaviorArchive> found = archiveRepository.findByPdiTaskId(9999L);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== 站点筛选查询测试 ====================

    @Test
    @DisplayName("站点筛选查询 - 指定站点")
    void findArchivesWithFilters_BySiteId() {
        // Given
        BehaviorArchive archive1 = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        BehaviorArchive archive2 = createArchive(
                2002L, 102L, 2L,
                LocalDateTime.of(2026, 3, 25, 9, 30, 0),
                LocalDateTime.of(2026, 3, 25, 9, 35, 0),
                15, 300, 2
        );
        entityManager.persist(archive1);
        entityManager.persist(archive2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<BehaviorArchive> page = archiveRepository.findArchivesWithFilters(
                1L, null, null, null, null, pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getSiteId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("状态筛选查询 - 指定状态")
    void findArchivesWithFilters_ByStatus() {
        // Given
        BehaviorArchive archive1 = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                null, 10, null, 0
        );
        BehaviorArchive archive2 = createArchive(
                2002L, 102L, 1L,
                LocalDateTime.of(2026, 3, 25, 9, 30, 0),
                LocalDateTime.of(2026, 3, 25, 9, 35, 0),
                15, 300, 2
        );
        entityManager.persist(archive1);
        entityManager.persist(archive2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<BehaviorArchive> page = archiveRepository.findArchivesWithFilters(
                null, 0, null, null, null, pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("时间范围筛选查询 - 指定时间范围")
    void findArchivesWithFilters_ByTimeRange() {
        // Given
        BehaviorArchive archive1 = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        BehaviorArchive archive2 = createArchive(
                2002L, 102L, 1L,
                LocalDateTime.of(2026, 3, 24, 9, 30, 0),
                LocalDateTime.of(2026, 3, 24, 9, 35, 0),
                15, 300, 2
        );
        entityManager.persist(archive1);
        entityManager.persist(archive2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<BehaviorArchive> page = archiveRepository.findArchivesWithFilters(
                null, null, null,
                LocalDateTime.of(2026, 3, 25, 0, 0, 0),
                LocalDateTime.of(2026, 3, 25, 23, 59, 59),
                pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStartTime().getDayOfMonth()).isEqualTo(25);
    }

    @Test
    @DisplayName("组合筛选查询 - 多个条件")
    void findArchivesWithFilters_CombinedFilters() {
        // Given
        BehaviorArchive archive1 = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        BehaviorArchive archive2 = createArchive(
                2002L, 102L, 1L,
                LocalDateTime.of(2026, 3, 25, 9, 30, 0),
                LocalDateTime.of(2026, 3, 25, 9, 35, 0),
                15, 300, 2
        );
        BehaviorArchive archive3 = createArchive(
                2003L, 101L, 2L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        entityManager.persist(archive1);
        entityManager.persist(archive2);
        entityManager.persist(archive3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 20);

        // When - 站点1 + 状态1 + 通道101
        Page<BehaviorArchive> page = archiveRepository.findArchivesWithFilters(
                1L, 1, 101L, null, null, pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPdiTaskId()).isEqualTo(2001L);
    }

    // ==================== 计数查询测试 ====================

    @Test
    @DisplayName("根据站点ID计数")
    void countBySiteIdAndDeletedAtIsNull() {
        // Given
        BehaviorArchive archive1 = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        BehaviorArchive archive2 = createArchive(
                2002L, 102L, 1L,
                LocalDateTime.of(2026, 3, 25, 9, 30, 0),
                LocalDateTime.of(2026, 3, 25, 9, 35, 0),
                15, 300, 2
        );
        BehaviorArchive archive3 = createArchive(
                2003L, 103L, 2L,
                LocalDateTime.of(2026, 3, 25, 8, 0, 0),
                LocalDateTime.of(2026, 3, 25, 8, 10, 0),
                10, 600, 1
        );
        entityManager.persist(archive1);
        entityManager.persist(archive2);
        entityManager.persist(archive3);
        entityManager.flush();

        // When
        long count = archiveRepository.countBySiteIdAndDeletedAtIsNull(1L);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("根据状态计数")
    void countByStatusAndDeletedAtIsNull() {
        // Given
        BehaviorArchive archive1 = createArchive(
                2001L, 101L, 1L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 25, 10, 8, 32),
                10, 512, 1
        );
        BehaviorArchive archive2 = createArchive(
                2002L, 102L, 1L,
                LocalDateTime.of(2026, 3, 25, 9, 30, 0),
                LocalDateTime.of(2026, 3, 25, 9, 35, 0),
                15, 300, 1
        );
        BehaviorArchive archive3 = createArchive(
                2003L, 103L, 1L,
                LocalDateTime.of(2026, 3, 25, 8, 0, 0),
                null, 10, null, 0
        );
        entityManager.persist(archive1);
        entityManager.persist(archive2);
        entityManager.persist(archive3);
        entityManager.flush();

        // When
        long count = archiveRepository.countByStatusAndDeletedAtIsNull(1);

        // Then
        assertThat(count).isEqualTo(2);
    }
}
