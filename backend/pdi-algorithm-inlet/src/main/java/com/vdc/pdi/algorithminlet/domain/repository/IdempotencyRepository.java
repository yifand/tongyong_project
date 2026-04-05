package com.vdc.pdi.algorithminlet.domain.repository;

import com.vdc.pdi.algorithminlet.domain.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 幂等记录数据访问层
 */
@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    /**
     * 检查指定时间之后是否存在该幂等键
     *
     * @param key       幂等键
     * @param afterTime 时间阈值
     * @return 是否存在
     */
    boolean existsByKeyAndCreatedAtAfter(String key, LocalDateTime afterTime);

    /**
     * 根据幂等键查询记录
     *
     * @param key 幂等键
     * @return 幂等记录
     */
    IdempotencyRecord findByKey(String key);

    /**
     * 删除过期的幂等记录
     *
     * @param expireTime 过期时间阈值
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM IdempotencyRecord r WHERE r.expireAt < :expireTime")
    int deleteByExpireAtBefore(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 删除指定时间之前创建的记录
     *
     * @param createdAt 创建时间阈值
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM IdempotencyRecord r WHERE r.createdAt < :createdAt")
    int deleteByCreatedAtBefore(@Param("createdAt") LocalDateTime createdAt);
}
