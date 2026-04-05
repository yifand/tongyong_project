package com.vdc.pdi.alarm.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAlarmRecord is a Querydsl query type for AlarmRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAlarmRecord extends EntityPathBase<AlarmRecord> {

    private static final long serialVersionUID = -1723676155L;

    public static final QAlarmRecord alarmRecord = new QAlarmRecord("alarmRecord");

    public final com.vdc.pdi.common.entity.QBaseEntity _super = new com.vdc.pdi.common.entity.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> alarmTime = createDateTime("alarmTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> channelId = createNumber("channelId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath extraInfo = createString("extraInfo");

    public final StringPath faceImageUrl = createString("faceImageUrl");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath location = createString("location");

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> processorId = createNumber("processorId", Long.class);

    public final StringPath remark = createString("remark");

    public final StringPath sceneImageUrl = createString("sceneImageUrl");

    //inherited
    public final NumberPath<Long> siteId = _super.siteId;

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAlarmRecord(String variable) {
        super(AlarmRecord.class, forVariable(variable));
    }

    public QAlarmRecord(Path<? extends AlarmRecord> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAlarmRecord(PathMetadata metadata) {
        super(AlarmRecord.class, metadata);
    }

}

