package com.vdc.pdi.logmanagement.domain.repository;

import com.vdc.pdi.logmanagement.domain.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 系统日志Repository
 */
@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long>,
        QuerydslPredicateExecutor<SystemLog> {

    /**
     * 批量删除指定时间之前的日志
     */
    @Modifying
    @Query("DELETE FROM SystemLog s WHERE s.createdAt < :before")
    int deleteByCreatedAtBefore(@Param("before") LocalDateTime before);
}
