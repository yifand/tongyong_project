package com.vdc.pdi.alarm.domain.repository;

import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import com.vdc.pdi.alarm.domain.vo.AlarmStatisticsVO;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 报警记录自定义查询接口
 */
public interface AlarmQueryRepository {

    /**
     * 查询最近报警
     */
    List<AlarmRecord> findRecentAlarms(Set<Long> siteIds, Integer type, Integer limit);

    /**
     * 条件分页查询
     */
    Page<AlarmRecord> findAll(Predicate predicate, Pageable pageable);

    /**
     * 统计查询
     */
    AlarmStatisticsVO getStatistics(Set<Long> siteIds, LocalDateTime startTime, LocalDateTime endTime);
}
