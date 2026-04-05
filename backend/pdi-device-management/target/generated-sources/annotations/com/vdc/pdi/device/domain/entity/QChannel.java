package com.vdc.pdi.device.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChannel is a Querydsl query type for Channel
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChannel extends EntityPathBase<Channel> {

    private static final long serialVersionUID = -1573853015L;

    public static final QChannel channel = new QChannel("channel");

    public final com.vdc.pdi.common.entity.QBaseEntity _super = new com.vdc.pdi.common.entity.QBaseEntity(this);

    public final StringPath algorithmType = createString("algorithmType");

    public final NumberPath<Long> boxId = createNumber("boxId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath name = createString("name");

    public final StringPath rtspUrl = createString("rtspUrl");

    //inherited
    public final NumberPath<Long> siteId = _super.siteId;

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChannel(String variable) {
        super(Channel.class, forVariable(variable));
    }

    public QChannel(Path<? extends Channel> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChannel(PathMetadata metadata) {
        super(Channel.class, metadata);
    }

}

