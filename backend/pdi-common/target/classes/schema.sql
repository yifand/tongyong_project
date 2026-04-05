-- ========================================================
-- PDI智能监测平台 - 数据库初始化脚本 (PostgreSQL)
-- Phase 1: 基础设施层
-- ========================================================

-- 站点表
CREATE TABLE IF NOT EXISTS site (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '站点名称',
    code VARCHAR(20) NOT NULL COMMENT '站点编码，如 JQ、KD',
    site_id BIGINT NOT NULL DEFAULT 0 COMMENT '站点ID（自身引用，用于数据隔离）',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间（逻辑删除）',
    created_by BIGINT COMMENT '创建人ID'
);
COMMENT ON TABLE site IS '站点信息表';
COMMENT ON COLUMN site.name IS '站点名称，如：金桥库、凯迪库';
COMMENT ON COLUMN site.code IS '站点编码，如：JQ、KD';
CREATE INDEX idx_site_code ON site(code);
CREATE INDEX idx_site_site_id ON site(site_id);

-- 边缘盒子表
CREATE TABLE IF NOT EXISTS edge_box (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL COMMENT '所属站点ID',
    name VARCHAR(100) NOT NULL COMMENT '盒子名称',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    status INTEGER NOT NULL DEFAULT 0 COMMENT '状态：0离线, 1在线',
    last_heartbeat_at TIMESTAMP COMMENT '最后心跳时间',
    version VARCHAR(50) COMMENT '软件版本号',
    cpu_usage DOUBLE PRECISION COMMENT 'CPU使用率',
    memory_usage DOUBLE PRECISION COMMENT '内存使用率',
    disk_usage DOUBLE PRECISION COMMENT '磁盘使用率',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    created_by BIGINT COMMENT '创建人ID'
);
COMMENT ON TABLE edge_box IS '边缘盒子设备表';
COMMENT ON COLUMN edge_box.status IS '设备状态：0离线, 1在线';
CREATE INDEX idx_edge_box_site_id ON edge_box(site_id);
CREATE INDEX idx_edge_box_status ON edge_box(status);

-- 视频通道表
CREATE TABLE IF NOT EXISTS channel (
    id BIGSERIAL PRIMARY KEY,
    box_id BIGINT NOT NULL COMMENT '所属盒子ID',
    site_id BIGINT NOT NULL COMMENT '所属站点ID',
    name VARCHAR(100) NOT NULL COMMENT '通道名称',
    type INTEGER NOT NULL DEFAULT 0 COMMENT '类型：0视频流, 1抓拍机',
    status INTEGER NOT NULL DEFAULT 0 COMMENT '状态：0离线, 1在线',
    algorithm_type VARCHAR(100) COMMENT '算法类型：smoke,pdi_left_front,pdi_left_rear,pdi_slide',
    rtsp_url VARCHAR(500) COMMENT 'RTSP地址（加密存储）',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    created_by BIGINT COMMENT '创建人ID'
);
COMMENT ON TABLE channel IS '视频通道表';
COMMENT ON COLUMN channel.type IS '通道类型：0视频流, 1抓拍机';
COMMENT ON COLUMN channel.status IS '通道状态：0离线, 1在线';
CREATE INDEX idx_channel_box_id ON channel(box_id);
CREATE INDEX idx_channel_site_id ON channel(site_id);
CREATE INDEX idx_channel_type ON channel(type);

-- 状态流原始数据表
CREATE TABLE IF NOT EXISTS state_stream (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL COMMENT '通道ID',
    site_id BIGINT NOT NULL COMMENT '站点ID',
    event_time TIMESTAMP NOT NULL COMMENT '边缘盒子上报时间',
    door_open INTEGER NOT NULL DEFAULT 0 COMMENT '门状态：0关, 1开',
    person_present INTEGER NOT NULL DEFAULT 0 COMMENT '人员状态：0无人, 1有人',
    entering_exiting INTEGER NOT NULL DEFAULT 0 COMMENT '进出状态：0未进出, 1进出中',
    state_code INTEGER NOT NULL COMMENT '状态码：S1=1, S3=3, S5=5, S7=7, S8=8',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    created_by BIGINT COMMENT '创建人ID'
);
COMMENT ON TABLE state_stream IS '状态流原始数据表';
COMMENT ON COLUMN state_stream.state_code IS '状态码：1空闲, 3门关有人, 5门开无人, 7门开有人, 8进出中';
CREATE INDEX idx_state_stream_channel_id ON state_stream(channel_id);
CREATE INDEX idx_state_stream_site_id ON state_stream(site_id);
CREATE INDEX idx_state_stream_event_time ON state_stream(event_time);
CREATE INDEX idx_state_stream_state_code ON state_stream(state_code);

-- PDI作业记录表
CREATE TABLE IF NOT EXISTS pdi_task (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL COMMENT '通道ID',
    site_id BIGINT NOT NULL COMMENT '站点ID',
    start_time TIMESTAMP COMMENT '作业开始时间',
    end_time TIMESTAMP COMMENT '作业结束时间',
    estimated_duration INTEGER COMMENT '标准工时（分钟）',
    actual_duration INTEGER COMMENT '实际时长（秒）',
    result INTEGER DEFAULT 0 COMMENT '结果：0进行中, 1达标, 2未达标',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    created_by BIGINT COMMENT '创建人ID'
);
COMMENT ON TABLE pdi_task IS 'PDI作业记录表';
COMMENT ON COLUMN pdi_task.result IS '作业结果：0进行中, 1达标, 2未达标';
CREATE INDEX idx_pdi_task_channel_id ON pdi_task(channel_id);
CREATE INDEX idx_pdi_task_site_id ON pdi_task(site_id);
CREATE INDEX idx_pdi_task_start_time ON pdi_task(start_time);

-- 报警记录表
CREATE TABLE IF NOT EXISTS alarm_record (
    id BIGSERIAL PRIMARY KEY,
    type INTEGER NOT NULL COMMENT '报警类型：0抽烟, 1PDI违规',
    site_id BIGINT NOT NULL COMMENT '站点ID',
    channel_id BIGINT NOT NULL COMMENT '通道ID',
    alarm_time TIMESTAMP NOT NULL COMMENT '报警时间',
    location VARCHAR(200) COMMENT '地点描述',
    face_image_url VARCHAR(500) COMMENT '人脸图片URL',
    scene_image_url VARCHAR(500) COMMENT '场景图片URL',
    status INTEGER NOT NULL DEFAULT 0 COMMENT '状态：0未处理, 1已处理, 2误报',
    extra_info TEXT COMMENT '扩展信息（JSON格式：时长/置信度等）',
    processor_id BIGINT COMMENT '处理人ID',
    processed_at TIMESTAMP COMMENT '处理时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP COMMENT '删除时间',
    created_by BIGINT COMMENT '创建人ID'
);
COMMENT ON TABLE alarm_record IS '报警记录表';
COMMENT ON COLUMN alarm_record.type IS '报警类型：0抽烟, 1PDI违规';
COMMENT ON COLUMN alarm_record.status IS '处理状态：0未处理, 1已处理, 2误报';
CREATE INDEX idx_alarm_site_id ON alarm_record(site_id);
CREATE INDEX idx_alarm_channel_id ON alarm_record(channel_id);
CREATE INDEX idx_alarm_type ON alarm_record(type);
CREATE INDEX idx_alarm_status ON alarm_record(status);
CREATE INDEX idx_alarm_alarm_time ON alarm_record(alarm_time);

-- 系统角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    site_id BIGINT NOT NULL DEFAULT 0 COMMENT '站点ID（0表示全局角色）',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT
);
COMMENT ON TABLE sys_role IS '系统角色表';
CREATE INDEX idx_sys_role_code ON sys_role(code);
CREATE INDEX idx_sys_role_site_id ON sys_role(site_id);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    site_id BIGINT NOT NULL COMMENT '站点ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT
);
COMMENT ON TABLE sys_user_role IS '用户角色关联表';
CREATE UNIQUE INDEX idx_user_role_unique ON sys_user_role(user_id, role_id, site_id);
CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    duration INTEGER COMMENT '执行时长（毫秒）',
    status INTEGER DEFAULT 1 COMMENT '状态：0失败, 1成功',
    error_msg TEXT COMMENT '错误信息',
    site_id BIGINT NOT NULL COMMENT '站点ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE sys_operation_log IS '操作日志表';
COMMENT ON COLUMN sys_operation_log.status IS '操作状态：0失败, 1成功';
CREATE INDEX idx_sys_operation_log_user_id ON sys_operation_log(user_id);
CREATE INDEX idx_sys_operation_log_site_id ON sys_operation_log(site_id);
CREATE INDEX idx_sys_operation_log_created_at ON sys_operation_log(created_at);
