package com.vdc.pdi.behaviorarchive.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBehaviorArchive is a Querydsl query type for BehaviorArchive
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBehaviorArchive extends EntityPathBase<BehaviorArchive> {

    private static final long serialVersionUID = 566670066L;

    public static final QBehaviorArchive behaviorArchive = new QBehaviorArchive("behaviorArchive");

    public final com.vdc.pdi.common.entity.QBaseEntity _super = new com.vdc.pdi.common.entity.QBaseEntity(this);

    public final NumberPath<Integer> actualDuration = createNumber("actualDuration", Integer.class);

    public final NumberPath<Long> channelId = createNumber("channelId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> estimatedDuration = createNumber("estimatedDuration", Integer.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Long> pdiTaskId = createNumber("pdiTaskId", Long.class);

    public final NumberPath<Long> siteId = createNumber("siteId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBehaviorArchive(String variable) {
        super(BehaviorArchive.class, forVariable(variable));
    }

    public QBehaviorArchive(Path<? extends BehaviorArchive> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBehaviorArchive(PathMetadata metadata) {
        super(BehaviorArchive.class, metadata);
    }

}

