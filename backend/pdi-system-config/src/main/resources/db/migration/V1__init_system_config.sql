-- =====================================================
-- PDI System Config Module - Database Migration
-- Version: V1
-- Description: 初始化系统配置模块数据表
-- =====================================================

-- -----------------------------------------------------
-- Table: algorithm_config
-- 算法配置表 - 存储通道和全局算法配置
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS algorithm_config (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    site_id             BIGINT NOT NULL DEFAULT 0 COMMENT '站点ID，0表示全局',
    channel_id          BIGINT COMMENT '通道ID，NULL表示全局配置',
    algorithm_type      VARCHAR(50) NOT NULL COMMENT '算法类型: SMOKE, PDI_LEFT_FRONT, PDI_LEFT_REAR, PDI_SLIDE',
    enabled             BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    sensitivity         VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '灵敏度: LOW, MEDIUM, HIGH',
    trigger_frames      INT DEFAULT 3 COMMENT '连续触发帧数',
    standard_duration   INT COMMENT '标准工时(秒)，PDI算法专用',
    enter_exit_window   INT DEFAULT 5 COMMENT '进出判定时间窗口(秒)',
    person_disappear_timeout INT DEFAULT 10 COMMENT '人员消失超时阈值(秒)',
    inherit_global      BOOLEAN DEFAULT TRUE COMMENT '是否继承全局配置',
    extra_params        TEXT COMMENT '扩展参数JSON',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at          DATETIME COMMENT '删除时间(软删除)',

    UNIQUE KEY uk_site_channel_algorithm (site_id, channel_id, algorithm_type),
    INDEX idx_site_id (site_id),
    INDEX idx_channel_id (channel_id),
    INDEX idx_algorithm_type (algorithm_type),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='算法配置表';

-- -----------------------------------------------------
-- Table: business_rule
-- 业务规则表 - 存储状态转换、标准工时、告警阈值等规则
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS business_rule (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    site_id             BIGINT NOT NULL DEFAULT 0 COMMENT '站点ID',
    rule_name           VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code           VARCHAR(50) NOT NULL COMMENT '规则编码',
    rule_type           VARCHAR(50) NOT NULL COMMENT '规则类型: STATE_TRANSITION, PDI_STANDARD_TIME, ALARM_THRESHOLD',
    rule_config         JSON NOT NULL COMMENT '规则配置JSON',
    enabled             BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    description         VARCHAR(500) COMMENT '规则描述',
    priority            INT DEFAULT 0 COMMENT '优先级，数值越大优先级越高',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at          DATETIME COMMENT '删除时间(软删除)',

    UNIQUE KEY uk_site_rule_code (site_id, rule_code),
    INDEX idx_site_id (site_id),
    INDEX idx_rule_type (rule_type),
    INDEX idx_enabled (enabled),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务规则表';

-- -----------------------------------------------------
-- Table: system_config
-- 系统配置表 - 存储通用系统配置项
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS system_config (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    site_id             BIGINT NOT NULL DEFAULT 0 COMMENT '站点ID',
    config_key          VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value        TEXT COMMENT '配置值',
    config_group        VARCHAR(50) NOT NULL COMMENT '配置分组',
    description         VARCHAR(500) COMMENT '配置描述',
    value_type          VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型: STRING, INTEGER, BOOLEAN, JSON',
    default_value       TEXT COMMENT '默认值',
    editable            BOOLEAN DEFAULT TRUE COMMENT '是否可编辑',
    builtin             BOOLEAN DEFAULT FALSE COMMENT '是否系统内置',
    sort_order          INT DEFAULT 0 COMMENT '排序顺序',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at          DATETIME COMMENT '删除时间(软删除)',

    UNIQUE KEY uk_site_config_key (site_id, config_key),
    INDEX idx_config_group (config_group),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- -----------------------------------------------------
-- 初始化业务规则数据
-- -----------------------------------------------------

-- 默认状态转换规则
INSERT INTO business_rule (site_id, rule_name, rule_code, rule_type, rule_config, enabled, description, priority) VALUES
(0, '默认状态转换规则', 'DEFAULT_STATE_TRANSITION', 'STATE_TRANSITION', '{
    "transitions": [
        {"fromState": "INIT", "toState": "WORKING", "condition": "人员进入", "description": "开始工作", "autoTransition": true, "timeoutSeconds": 0},
        {"fromState": "WORKING", "toState": "PAUSE", "condition": "人员离开", "description": "暂停计时", "autoTransition": true, "timeoutSeconds": 0},
        {"fromState": "PAUSE", "toState": "WORKING", "condition": "人员返回", "description": "恢复计时", "autoTransition": true, "timeoutSeconds": 0},
        {"fromState": "WORKING", "toState": "COMPLETED", "condition": "工作完成", "description": "正常完成", "autoTransition": false, "timeoutSeconds": 0}
    ]
}', TRUE, 'PDI作业默认状态转换规则', 0);

-- 默认PDI标准工时配置
INSERT INTO business_rule (site_id, rule_name, rule_code, rule_type, rule_config, enabled, description, priority) VALUES
(0, '默认PDI标准工时配置', 'DEFAULT_PDI_TIME', 'PDI_STANDARD_TIME', '{
    "defaultStandardDuration": 300,
    "overtimeThresholdPercent": 120,
    "channelConfigs": []
}', TRUE, 'PDI作业默认标准工时配置', 0);

-- 默认告警阈值配置
INSERT INTO business_rule (site_id, rule_name, rule_code, rule_type, rule_config, enabled, description, priority) VALUES
(0, '默认告警阈值配置', 'DEFAULT_ALARM_THRESHOLD', 'ALARM_THRESHOLD', '{
    "suppressSeconds": 300,
    "maxAlarmCount": 0,
    "levelThresholds": [
        {"level": "INFO", "durationThreshold": 0, "countThreshold": 1, "escalationSeconds": 0},
        {"level": "WARNING", "durationThreshold": 60, "countThreshold": 3, "escalationSeconds": 300},
        {"level": "ERROR", "durationThreshold": 180, "countThreshold": 5, "escalationSeconds": 600},
        {"level": "CRITICAL", "durationThreshold": 300, "countThreshold": 10, "escalationSeconds": 900}
    ]
}', TRUE, '告警级别阈值默认配置', 0);

-- -----------------------------------------------------
-- 初始化系统配置数据
-- -----------------------------------------------------

-- 系统基础配置
INSERT INTO system_config (site_id, config_key, config_value, config_group, description, value_type, default_value, editable, builtin, sort_order) VALUES
(0, 'system.name', 'PDI智能监控平台', 'system', '系统名称', 'STRING', 'PDI智能监控平台', FALSE, TRUE, 1),
(0, 'system.version', '1.0.0', 'system', '系统版本', 'STRING', '1.0.0', FALSE, TRUE, 2),
(0, 'system.debug', 'false', 'system', '调试模式', 'BOOLEAN', 'false', TRUE, TRUE, 3);

-- 告警通知配置
INSERT INTO system_config (site_id, config_key, config_value, config_group, description, value_type, default_value, editable, builtin, sort_order) VALUES
(0, 'alarm.notification.enabled', 'true', 'alarm', '启用告警通知', 'BOOLEAN', 'true', TRUE, TRUE, 1),
(0, 'alarm.notification.channels', '["WEBHOOK", "SMS"]', 'alarm', '通知渠道列表', 'JSON', '["WEBHOOK"]', TRUE, TRUE, 2),
(0, 'alarm.retention.days', '30', 'alarm', '告警数据保留天数', 'INTEGER', '30', TRUE, TRUE, 3);

-- 视频流配置
INSERT INTO system_config (site_id, config_key, config_value, config_group, description, value_type, default_value, editable, builtin, sort_order) VALUES
(0, 'stream.default_fps', '25', 'stream', '默认视频帧率', 'INTEGER', '25', TRUE, TRUE, 1),
(0, 'stream.default_resolution', '1920x1080', 'stream', '默认视频分辨率', 'STRING', '1920x1080', TRUE, TRUE, 2),
(0, 'stream.buffer_size', '100', 'stream', '视频缓冲区大小', 'INTEGER', '100', TRUE, TRUE, 3);

-- 数据保留策略
INSERT INTO system_config (site_id, config_key, config_value, config_group, description, value_type, default_value, editable, builtin, sort_order) VALUES
(0, 'retention.detect_result.days', '90', 'retention', '检测结果保留天数', 'INTEGER', '90', TRUE, TRUE, 1),
(0, 'retention.operation_log.days', '365', 'retention', '操作日志保留天数', 'INTEGER', '365', TRUE, TRUE, 2),
(0, 'retention.heartbeat.days', '7', 'retention', '心跳数据保留天数', 'INTEGER', '7', TRUE, TRUE, 3);
