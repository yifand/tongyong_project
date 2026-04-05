package com.vdc.pdi.device.domain.repository;

import com.vdc.pdi.device.domain.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 通道数据访问层
 */
@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long>, QuerydslPredicateExecutor<Channel> {

    /**
     * 根据ID查询未删除的通道
     */
    Optional<Channel> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 根据盒子ID查询通道
     */
    List<Channel> findByBoxIdAndDeletedAtIsNull(Long boxId);

    /**
     * 根据站点ID查询通道
     */
    List<Channel> findBySiteIdAndDeletedAtIsNull(Long siteId);

    /**
     * 根据算法类型查询通道
     */
    List<Channel> findByAlgorithmTypeAndDeletedAtIsNull(String algorithmType);

    /**
     * 根据站点ID和算法类型查询通道
     */
    List<Channel> findBySiteIdAndAlgorithmTypeAndDeletedAtIsNull(Long siteId, String algorithmType);

    /**
     * 统计盒子通道数量
     */
    long countByBoxIdAndDeletedAtIsNull(Long boxId);

    /**
     * 统计站点通道数量
     */
    long countBySiteIdAndDeletedAtIsNull(Long siteId);

    /**
     * 统计站点特定状态的通道数量
     */
    long countBySiteIdAndStatusAndDeletedAtIsNull(Long siteId, Integer status);

    /**
     * 统计站点算法通道数量
     */
    @Query("SELECT c.algorithmType, COUNT(c) FROM Channel c WHERE c.siteId = :siteId AND c.deletedAt IS NULL GROUP BY c.algorithmType")
    List<Object[]> countBySiteIdGroupByAlgorithmType(@Param("siteId") Long siteId);

    /**
     * 根据名称模糊查询
     */
    List<Channel> findByNameContainingAndDeletedAtIsNull(String name);

    /**
     * 根据站点ID、盒子ID和算法类型查询
     */
    List<Channel> findBySiteIdAndBoxIdAndAlgorithmTypeAndDeletedAtIsNull(Long siteId, Long boxId, String algorithmType);
}
