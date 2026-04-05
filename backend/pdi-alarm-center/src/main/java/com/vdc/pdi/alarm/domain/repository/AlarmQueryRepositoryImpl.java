package com.vdc.pdi.alarm.domain.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import com.vdc.pdi.alarm.domain.entity.QAlarmRecord;
import com.vdc.pdi.alarm.domain.vo.AlarmStatisticsVO;
import com.vdc.pdi.common.enums.AlarmStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 报警记录自定义查询实现
 */
@Repository
@RequiredArgsConstructor
public class AlarmQueryRepositoryImpl implements AlarmQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AlarmRecord> findRecentAlarms(Set<Long> siteIds, Integer type, Integer limit) {
        QAlarmRecord alarm = QAlarmRecord.alarmRecord;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(alarm.siteId.in(siteIds));
        builder.and(alarm.deletedAt.isNull());

        if (type != null) {
            builder.and(alarm.type.eq(type));
        }

        return queryFactory
                .selectFrom(alarm)
                .where(builder)
                .orderBy(alarm.alarmTime.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<AlarmRecord> findAll(Predicate predicate, Pageable pageable) {
        QAlarmRecord alarm = QAlarmRecord.alarmRecord;

        JPAQuery<AlarmRecord> query = queryFactory
                .selectFrom(alarm)
                .where(predicate);

        // 执行分页查询
        List<AlarmRecord> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(alarm.alarmTime.desc())
                .fetch();

        // 查询总数
        Long total = queryFactory
                .select(alarm.count())
                .from(alarm)
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public AlarmStatisticsVO getStatistics(Set<Long> siteIds, LocalDateTime startTime, LocalDateTime endTime) {
        QAlarmRecord alarm = QAlarmRecord.alarmRecord;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(alarm.siteId.in(siteIds));
        builder.and(alarm.alarmTime.goe(startTime));
        builder.and(alarm.alarmTime.lt(endTime));
        builder.and(alarm.deletedAt.isNull());

        // 排除误报
        builder.and(alarm.status.ne(AlarmStatusEnum.FALSE_POSITIVE.getCode()));

        List<Tuple> results = queryFactory
                .select(
                        alarm.count(),
                        alarm.status.when(AlarmStatusEnum.UNPROCESSED.getCode()).then(1).otherwise(0).sum(),
                        alarm.status.when(AlarmStatusEnum.PROCESSED.getCode()).then(1).otherwise(0).sum()
                )
                .from(alarm)
                .where(builder)
                .fetch();

        Tuple tuple = results.get(0);

        Long total = tuple.get(0, Long.class);
        Long unprocessed = tuple.get(1, Long.class);
        Long processed = tuple.get(2, Long.class);

        // 处理可能的null值
        total = total != null ? total : 0L;
        unprocessed = unprocessed != null ? unprocessed : 0L;
        processed = processed != null ? processed : 0L;

        return AlarmStatisticsVO.builder()
                .total(total)
                .unprocessed(unprocessed)
                .processed(processed)
                .build();
    }
}
