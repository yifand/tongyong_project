package com.vdc.pdi.behaviorarchive.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStateStream is a Querydsl query type for StateStream
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStateStream extends EntityPathBase<StateStream> {

    private static final long serialVersionUID = 1472666963L;

    public static final QStateStream stateStream = new QStateStream("stateStream");

    public final com.vdc.pdi.common.entity.QBaseEntity _super = new com.vdc.pdi.common.entity.QBaseEntity(this);

    public final NumberPath<Long> channelId = createNumber("channelId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Integer> doorOpen = createNumber("doorOpen", Integer.class);

    public final NumberPath<Integer> enteringExiting = createNumber("enteringExiting", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> eventTime = createDateTime("eventTime", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Integer> personPresent = createNumber("personPresent", Integer.class);

    public final NumberPath<Long> siteId = createNumber("siteId", Long.class);

    public final NumberPath<Integer> stateCode = createNumber("stateCode", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStateStream(String variable) {
        super(StateStream.class, forVariable(variable));
    }

    public QStateStream(Path<? extends StateStream> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStateStream(PathMetadata metadata) {
        super(StateStream.class, metadata);
    }

}

