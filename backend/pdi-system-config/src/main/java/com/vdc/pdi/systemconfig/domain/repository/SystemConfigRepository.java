package com.vdc.pdi.systemconfig.domain.repository;

import com.vdc.pdi.systemconfig.domain.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置Repository
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * 根据配置键查询
     */
    Optional<SystemConfig> findByConfigKeyAndDeletedAtIsNull(String configKey);

    /**
     * 根据配置分组查询
     */
    List<SystemConfig> findByConfigGroupAndDeletedAtIsNullOrderBySortOrderAsc(String configGroup);

    /**
     * 查询所有未删除的配置
     */
    List<SystemConfig> findAllByDeletedAtIsNullOrderByConfigGroupAscSortOrderAsc();

    /**
     * 根据站点ID和配置键查询
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.siteId = :siteId AND sc.configKey = :configKey AND sc.deletedAt IS NULL")
    Optional<SystemConfig> findBySiteIdAndConfigKey(@Param("siteId") Long siteId, @Param("configKey") String configKey);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKeyAndDeletedAtIsNull(String configKey);

    /**
     * 根据配置键删除（逻辑删除）
     */
    @Query("UPDATE SystemConfig sc SET sc.deletedAt = CURRENT_TIMESTAMP WHERE sc.configKey = :configKey")
    void deleteByConfigKey(@Param("configKey") String configKey);
}
