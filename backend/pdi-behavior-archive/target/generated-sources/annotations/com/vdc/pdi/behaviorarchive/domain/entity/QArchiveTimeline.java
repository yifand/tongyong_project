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

    public final StringPath action = createString("action");

    public final NumberPath<Long> archiveId = createNumber("archiveId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> createdBy = createNumber("createdBy", Long.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> eventTime = createDateTime("eventTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Integer> seq = createNumber("seq", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

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

