-- ============================================
-- 设备管理模块数据库脚本
-- PDI智能监测平台
-- ============================================

-- 边缘盒子表
CREATE TABLE IF NOT EXISTS edge_box (
    id                      BIGSERIAL PRIMARY KEY COMMENT '主键ID',
    site_id                 BIGINT NOT NULL COMMENT '所属站点ID',
    name                    VARCHAR(64) NOT NULL COMMENT '盒子名称',
    ip_address              VARCHAR(15) NOT NULL COMMENT 'IP地址',
    status                  SMALLINT NOT NULL DEFAULT 0 COMMENT '状态: 0-离线, 1-在线',
    last_heartbeat_at       TIMESTAMP COMMENT '最后心跳时间',
    version                 VARCHAR(32) COMMENT '软件版本号',
    cpu_usage               DECIMAL(5,2) COMMENT 'CPU使用率(%)',
    memory_usage            DECIMAL(5,2) COMMENT '内存使用率(%)',
    disk_usage              DECIMAL(5,2) COMMENT '磁盘使用率(%)',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at              TIMESTAMP COMMENT '删除时间(逻辑删除)',
    created_by              BIGINT COMMENT '创建人ID'
);

-- 注释
COMMENT ON TABLE edge_box IS '边缘计算盒子表';

-- 索引
CREATE INDEX IF NOT EXISTS idx_edge_box_site_id ON edge_box(site_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_edge_box_status ON edge_box(status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_edge_box_heartbeat ON edge_box(last_heartbeat_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_edge_box_site_status ON edge_box(site_id, status) WHERE deleted_at IS NULL;

-- 唯一约束：站点内盒子名称唯一
CREATE UNIQUE INDEX IF NOT EXISTS uk_edge_box_site_name ON edge_box(site_id, name) WHERE deleted_at IS NULL;


-- 通道表
CREATE TABLE IF NOT EXISTS channel (
    id                      BIGSERIAL PRIMARY KEY COMMENT '主键ID',
    box_id                  BIGINT NOT NULL COMMENT '所属盒子ID',
    site_id                 BIGINT NOT NULL COMMENT '所属站点ID',
    name                    VARCHAR(64) NOT NULL COMMENT '通道名称',
    type                    SMALLINT NOT NULL DEFAULT 0 COMMENT '类型: 0-视频流, 1-抓拍机',
    status                  SMALLINT NOT NULL DEFAULT 0 COMMENT '状态: 0-离线, 1-在线',
    algorithm_type          VARCHAR(64) COMMENT '算法类型: smoke/pdi_left_front/pdi_left_rear/pdi_slide',
    rtsp_url                VARCHAR(512) COMMENT 'RTSP地址(加密存储)',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at              TIMESTAMP COMMENT '删除时间(逻辑删除)',
    created_by              BIGINT COMMENT '创建人ID'
);

-- 注释
COMMENT ON TABLE channel IS '视频通道表';
COMMENT ON COLUMN channel.algorithm_type IS '算法类型: smoke-抽烟检测, pdi_left_front-PDI左前门, pdi_left_rear-PDI左后门, pdi_slide-PDI滑移门';

-- 索引
CREATE INDEX IF NOT EXISTS idx_channel_box_id ON channel(box_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_channel_site_id ON channel(site_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_channel_status ON channel(status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_channel_algorithm ON channel(algorithm_type) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_channel_box_site ON channel(box_id, site_id) WHERE deleted_at IS NULL;
