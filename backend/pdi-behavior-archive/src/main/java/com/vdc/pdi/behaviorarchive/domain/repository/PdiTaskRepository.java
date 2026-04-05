package com.vdc.pdi.behaviorarchive.domain.repository;

import com.vdc.pdi.behaviorarchive.domain.entity.PdiTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PDI任务Repository
 */
@Repository
public interface PdiTaskRepository extends JpaRepository<PdiTask, Long> {

    /**
     * 根据通道ID查询最新的PDI任务
     */
    Optional<PdiTask> findFirstByChannelIdOrderByStartTimeDesc(Long channelId);
}
