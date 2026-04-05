package com.vdc.pdi.device.domain.repository;

import com.vdc.pdi.device.domain.entity.EdgeBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 边缘盒子数据访问层
 */
@Repository
public interface EdgeBoxRepository extends JpaRepository<EdgeBox, Long>, QuerydslPredicateExecutor<EdgeBox> {

    /**
     * 根据ID查询未删除的盒子
     */
    Optional<EdgeBox> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 根据站点ID查询盒子列表
     */
    List<EdgeBox> findBySiteIdAndDeletedAtIsNull(Long siteId);

    /**
     * 查询所有未删除的盒子
     */
    List<EdgeBox> findAllByDeletedAtIsNull();

    /**
     * 根据状态查询盒子
     */
    List<EdgeBox> findByStatusAndDeletedAtIsNull(Integer status);

    /**
     * 根据站点ID和状态查询盒子
     */
    List<EdgeBox> findBySiteIdAndStatusAndDeletedAtIsNull(Long siteId, Integer status);

    /**
     * 统计站点盒子数量
     */
    long countBySiteIdAndDeletedAtIsNull(Long siteId);

    /**
     * 统计站点在线盒子数量
     */
    long countBySiteIdAndStatusAndDeletedAtIsNull(Long siteId, Integer status);

    /**
     * 查询心跳超时的盒子
     */
    @Query("SELECT b FROM EdgeBox b WHERE b.status = 1 AND b.lastHeartbeatAt < :timeoutTime AND b.deletedAt IS NULL")
    List<EdgeBox> findTimeoutBoxes(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 根据名称模糊查询
     */
    List<EdgeBox> findByNameContainingAndDeletedAtIsNull(String name);

    /**
     * 根据IP地址查询
     */
    Optional<EdgeBox> findByIpAddressAndDeletedAtIsNull(String ipAddress);
}
