package com.vdc.pdi.behaviorarchive.domain.repository;

import com.vdc.pdi.behaviorarchive.domain.entity.ArchiveTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 档案时间线Repository
 */
@Repository
public interface ArchiveTimelineRepository extends JpaRepository<ArchiveTimeline, Long> {

    /**
     * 根据档案ID查询时间线节点
     */
    List<ArchiveTimeline> findByArchiveId(Long archiveId);

    /**
     * 根据档案ID查询时间线节点（按seq排序）
     */
    List<ArchiveTimeline> findByArchiveIdOrderBySeqAsc(Long archiveId);

    /**
     * 根据档案ID和seq大于指定值查询（按seq排序）
     */
    List<ArchiveTimeline> findByArchiveIdAndSeqGreaterThanOrderBySeqAsc(Long archiveId, Integer seq);

    /**
     * 根据档案ID查询最大seq
     */
    @Query("SELECT MAX(t.seq) FROM ArchiveTimeline t WHERE t.archiveId = :archiveId AND t.deletedAt IS NULL")
    Optional<Integer> findMaxSeqByArchiveId(@Param("archiveId") Long archiveId);

    /**
     * 根据档案ID删除所有时间线节点（逻辑删除）
     */
    @Query("UPDATE ArchiveTimeline t SET t.deletedAt = CURRENT_TIMESTAMP WHERE t.archiveId = :archiveId")
    void deleteByArchiveId(@Param("archiveId") Long archiveId);
}
