package com.vdc.pdi.behaviorarchive.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QArchiveTimeline is a Querydsl query type for ArchiveTimeline
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArchiveTimeline extends EntityPathBase<ArchiveTimeline> {

    private static final long serialVersionUID = -367524859L;

    public static final QArchiveTimeline archiveTimeline = new QArchiveTimeline("archiveTimeline");

    public final com.vdc.pdi.common.entity.QBaseEntity _super = new com.vdc.pdi.common.entity.QBaseEntity(this);

    public final StringPath action = createString("action");

    public final NumberPath<Long> archiveId = createNumber("archiveId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final DateTimePath<java.time.LocalDateTime> eventTime = createDateTime("eventTime", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Integer> seq = createNumber("seq", Integer.class);

    //inherited
    public final NumberPath<Long> siteId = _super.siteId;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QArchiveTimeline(String variable) {
        super(ArchiveTimeline.class, forVariable(variable));
    }

    public QArchiveTimeline(Path<? extends ArchiveTimeline> path) {
        super(path.getType(), path.getMetadata());
    }

    public QArchiveTimeline(PathMetadata metadata) {
        super(ArchiveTimeline.class, metadata);
    }

}

