package com.vdc.pdi.systemconfig.domain.repository;

import com.vdc.pdi.systemconfig.domain.entity.AlgorithmConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 算法配置Repository
 */
@Repository
public interface AlgorithmConfigRepository extends JpaRepository<AlgorithmConfig, Long> {

    /**
     * 根据站点ID、通道ID和算法类型查询
     */
    Optional<AlgorithmConfig> findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
            Long siteId, Long channelId, String algorithmType);

    /**
     * 根据通道ID查询
     */
    List<AlgorithmConfig> findByChannelIdAndDeletedAtIsNull(Long channelId);

    /**
     * 根据站点ID和通道ID查询所有配置（分页）
     */
    Page<AlgorithmConfig> findBySiteIdAndChannelIdAndDeletedAtIsNull(Long siteId, Long channelId, Pageable pageable);

    /**
     * 查询全局配置（channelId为null）
     */
    @Query("SELECT ac FROM AlgorithmConfig ac WHERE ac.siteId = :siteId AND ac.channelId IS NULL AND ac.deletedAt IS NULL")
    List<AlgorithmConfig> findGlobalConfigs(@Param("siteId") Long siteId);

    /**
     * 根据站点ID和算法类型查询全局配置
     */
    @Query("SELECT ac FROM AlgorithmConfig ac WHERE ac.siteId = :siteId AND ac.channelId IS NULL AND ac.algorithmType = :algorithmType AND ac.deletedAt IS NULL")
    Optional<AlgorithmConfig> findGlobalConfigByAlgorithmType(@Param("siteId") Long siteId, @Param("algorithmType") String algorithmType);

    /**
     * 查询启用的配置
     */
    List<AlgorithmConfig> findByEnabledAndDeletedAtIsNull(Boolean enabled);

    /**
     * 根据通道ID和启用状态查询
     */
    List<AlgorithmConfig> findByChannelIdAndEnabledAndDeletedAtIsNull(Long channelId, Boolean enabled);

    /**
     * 根据站点ID、算法类型和启用状态查询
     */
    List<AlgorithmConfig> findBySiteIdAndAlgorithmTypeAndEnabledAndDeletedAtIsNull(
            Long siteId, String algorithmType, Boolean enabled);

    /**
     * 检查配置是否存在
     */
    boolean existsBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
            Long siteId, Long channelId, String algorithmType);

    /**
     * 根据站点ID查询（分页）
     */
    Page<AlgorithmConfig> findBySiteIdAndDeletedAtIsNull(Long siteId, Pageable pageable);

    /**
     * 根据站点ID和算法类型查询（分页）
     */
    Page<AlgorithmConfig> findBySiteIdAndAlgorithmTypeAndDeletedAtIsNull(Long siteId, String algorithmType, Pageable pageable);

    /**
     * 查询站点全局配置（channelId为null，分页）
     */
    @Query("SELECT ac FROM AlgorithmConfig ac WHERE ac.siteId = :siteId AND ac.channelId IS NULL AND ac.deletedAt IS NULL")
    Page<AlgorithmConfig> findBySiteIdAndChannelIdIsNullAndDeletedAtIsNull(@Param("siteId") Long siteId, Pageable pageable);
}
