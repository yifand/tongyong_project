package com.vdc.pdi.systemconfig.domain.repository;

import com.vdc.pdi.systemconfig.domain.entity.BusinessRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 业务规则Repository
 */
@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRule, Long> {

    /**
     * 根据站点ID查询规则（分页）
     */
    Page<BusinessRule> findBySiteIdAndDeletedAtIsNull(Long siteId, Pageable pageable);

    /**
     * 根据站点ID和规则类型查询（分页）
     */
    Page<BusinessRule> findBySiteIdAndRuleTypeAndDeletedAtIsNull(Long siteId, String ruleType, Pageable pageable);

    /**
     * 根据站点ID和规则编码查询
     */
    Optional<BusinessRule> findBySiteIdAndRuleCodeAndDeletedAtIsNull(Long siteId, String ruleCode);

    /**
     * 根据规则编码查询
     */
    Optional<BusinessRule> findByRuleCodeAndDeletedAtIsNull(String ruleCode);

    /**
     * 根据规则类型查询
     */
    List<BusinessRule> findByRuleTypeAndDeletedAtIsNullOrderByPriorityAsc(String ruleType);

    /**
     * 查询所有启用的规则
     */
    List<BusinessRule> findByEnabledAndDeletedAtIsNullOrderByPriorityAsc(Boolean enabled);

    /**
     * 根据规则类型和启用状态查询
     */
    List<BusinessRule> findByRuleTypeAndEnabledAndDeletedAtIsNullOrderByPriorityAsc(String ruleType, Boolean enabled);

    /**
     * 根据站点ID和规则类型查询启用的规则
     */
    List<BusinessRule> findBySiteIdAndRuleTypeAndEnabledAndDeletedAtIsNullOrderByPriorityAsc(
            Long siteId, String ruleType, Boolean enabled);

    /**
     * 查询特定类型的有效规则（用于规则引擎）
     * 按优先级排序，返回第一条
     */
    @Query("SELECT br FROM BusinessRule br WHERE br.siteId = :siteId AND br.ruleType = :ruleType AND br.enabled = true AND br.deletedAt IS NULL ORDER BY br.priority ASC")
    Optional<BusinessRule> findActiveRuleByType(@Param("siteId") Long siteId, @Param("ruleType") String ruleType);

    /**
     * 检查规则编码是否存在
     */
    boolean existsByRuleCodeAndDeletedAtIsNull(String ruleCode);

    /**
     * 检查站点下规则编码是否存在
     */
    boolean existsBySiteIdAndRuleCodeAndDeletedAtIsNull(Long siteId, String ruleCode);
}
