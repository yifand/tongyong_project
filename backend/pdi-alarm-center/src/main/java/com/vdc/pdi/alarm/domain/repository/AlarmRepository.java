package com.vdc.pdi.alarm.domain.repository;

import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * 报警记录数据访问层
 */
@Repository
public interface AlarmRepository extends JpaRepository<AlarmRecord, Long>,
        QuerydslPredicateExecutor<AlarmRecord>, AlarmQueryRepository {
}
