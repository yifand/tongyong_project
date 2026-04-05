package com.vdc.pdi.behaviorarchive.domain.repository;

import com.vdc.pdi.behaviorarchive.domain.entity.StateStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 状态流Repository
 */
@Repository
public interface StateStreamRepository extends JpaRepository<StateStream, Long> {

    /**
     * 根据通道ID、状态码和事件时间查询第一条记录（按时间升序）
     */
    Optional<StateStream> findFirstByChannelIdAndStateCodeAndEventTimeGreaterThanEqualOrderByEventTimeAsc(
            Long channelId, Integer stateCode, LocalDateTime eventTime);

    /**
     * 根据通道ID、状态码列表和事件时间查询第一条记录（按时间降序）
     */
    Optional<StateStream> findFirstByChannelIdAndStateCodeInAndEventTimeLessThanEqualOrderByEventTimeDesc(
            Long channelId, Collection<Integer> stateCodes, LocalDateTime eventTime);

    /**
     * 根据通道ID和时间范围查询状态流
     */
    List<StateStream> findByChannelIdAndEventTimeBetweenOrderByEventTimeAsc(
            Long channelId, LocalDateTime startTime, LocalDateTime endTime);
}
