package com.vdc.pdi.logmanagement.domain.repository;

import com.vdc.pdi.logmanagement.domain.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 操作日志Repository
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>,
        QuerydslPredicateExecutor<OperationLog> {

    /**
     * 批量删除指定时间之前的日志
     */
    @Modifying
    @Query("DELETE FROM OperationLog o WHERE o.createdAt < :before")
    int deleteByCreatedAtBefore(@Param("before") LocalDateTime before);

    /**
     * 统计指定用户的操作次数
     */
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
