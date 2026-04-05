package com.vdc.pdi.behaviorarchive.domain.repository;

import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 行为档案Repository
 */
@Repository
public interface BehaviorArchiveRepository extends JpaRepository<BehaviorArchive, Long> {

    /**
     * 根据PDI任务ID查询档案
     */
    Optional<BehaviorArchive> findByPdiTaskId(Long pdiTaskId);

    /**
     * 根据PDI任务ID查询档案（包含已删除）
     */
    @Query("SELECT a FROM BehaviorArchive a WHERE a.pdiTaskId = :pdiTaskId")
    Optional<BehaviorArchive> findByPdiTaskIdIncludingDeleted(@Param("pdiTaskId") Long pdiTaskId);

    /**
     * 分页查询档案列表（带筛选条件）
     */
    @Query("SELECT a FROM BehaviorArchive a " +
           "WHERE a.deletedAt IS NULL " +
           "AND (:siteId IS NULL OR a.siteId = :siteId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:channelId IS NULL OR a.channelId = :channelId) " +
           "AND (:startTimeFrom IS NULL OR a.startTime >= :startTimeFrom) " +
           "AND (:startTimeTo IS NULL OR a.startTime <= :startTimeTo) " +
           "ORDER BY a.startTime DESC")
    Page<BehaviorArchive> findArchivesWithFilters(
            @Param("siteId") Long siteId,
            @Param("status") Integer status,
            @Param("channelId") Long channelId,
            @Param("startTimeFrom") LocalDateTime startTimeFrom,
            @Param("startTimeTo") LocalDateTime startTimeTo,
            Pageable pageable);

    /**
     * 根据站点ID查询档案数量
     */
    long countBySiteIdAndDeletedAtIsNull(Long siteId);

    /**
     * 根据状态查询档案数量
     */
    long countByStatusAndDeletedAtIsNull(Integer status);
}
