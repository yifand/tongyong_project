package com.vdc.pdi.logmanagement.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOperationLog is a Querydsl query type for OperationLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOperationLog extends EntityPathBase<OperationLog> {

    private static final long serialVersionUID = 738464708L;

    public static final QOperationLog operationLog = new QOperationLog("operationLog");

    public final com.vdc.pdi.common.entity.QBaseEntity _super = new com.vdc.pdi.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath errorMsg = createString("errorMsg");

    public final NumberPath<Long> executionTime = createNumber("executionTime", Long.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath ipAddress = createString("ipAddress");

    public final StringPath operationDetail = createString("operationDetail");

    public final NumberPath<Integer> operationType = createNumber("operationType", Integer.class);

    public final StringPath requestParams = createString("requestParams");

    public final NumberPath<Integer> result = createNumber("result", Integer.class);

    //inherited
    public final NumberPath<Long> siteId = _super.siteId;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath username = createString("username");

    public QOperationLog(String variable) {
        super(OperationLog.class, forVariable(variable));
    }

    public QOperationLog(Path<? extends OperationLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOperationLog(PathMetadata metadata) {
        super(OperationLog.class, metadata);
    }

}

