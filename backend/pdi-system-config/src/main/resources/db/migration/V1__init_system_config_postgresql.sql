-- =====================================================
-- PDI System Config Module - Database Migration (PostgreSQL)
-- Version: V1
-- Description: 初始化系统配置模块数据表 (PostgreSQL版本)
-- =====================================================

-- -----------------------------------------------------
-- Table: algorithm_config
-- 算法配置表 - 存储通道和全局算法配置
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS algorithm_config (
    id                  BIGSERIAL PRIMARY KEY,
    site_id             BIGINT NOT NULL DEFAULT 0,
    channel_id          BIGINT,
    algorithm_type      VARCHAR(50) NOT NULL,
    enabled             BOOLEAN DEFAULT TRUE,
    sensitivity         VARCHAR(20) DEFAULT 'MEDIUM',
    trigger_frames      INTEGER DEFAULT 3,
    standard_duration   INTEGER,
    enter_exit_window   INTEGER DEFAULT 5,
    person_disappear_timeout INTEGER DEFAULT 10,
    inherit_global      BOOLEAN DEFAULT TRUE,
    extra_params        JSONB,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT uk_site_channel_algorithm UNIQUE (site_id, channel_id, algorithm_type)
);

-- 索引
CREATE INDEX idx_algorithm_site_id ON algorithm_config(site_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_algorithm_channel_id ON algorithm_config(channel_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_algorithm_type ON algorithm_config(algorithm_type) WHERE deleted_at IS NULL;

-- 表注释
COMMENT ON TABLE algorithm_config IS '算法配置表 - 存储通道和全局算法配置';
COMMENT ON COLUMN algorithm_config.site_id IS '站点ID，0表示全局';
COMMENT ON COLUMN algorithm_config.channel_id IS '通道ID，NULL表示全局配置';
COMMENT ON COLUMN algorithm_config.algorithm_type IS '算法类型: SMOKE, PDI_LEFT_FRONT, PDI_LEFT_REAR, PDI_SLIDE';
COMMENT ON COLUMN algorithm_config.enabled IS '是否启用';
COMMENT ON COLUMN algorithm_config.sensitivity IS '灵敏度: LOW, MEDIUM, HIGH';
COMMENT ON COLUMN algorithm_config.trigger_frames IS '连续触发帧数';
COMMENT ON COLUMN algorithm_config.standard_duration IS '标准工时(秒)，PDI算法专用';
COMMENT ON COLUMN algorithm_config.enter_exit_window IS '进出判定时间窗口(秒)';
COMMENT ON COLUMN algorithm_config.person_disappear_timeout IS '人员消失超时阈值(秒)';
COMMENT ON COLUMN algorithm_config.inherit_global IS '是否继承全局配置';
COMMENT ON COLUMN algorithm_config.extra_params IS '扩展参数JSON';

-- -----------------------------------------------------
-- Table: business_rule
-- 业务规则表 - 存储状态转换、标准工时、告警阈值等规则
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS business_rule (
    id                  BIGSERIAL PRIMARY KEY,
    site_id             BIGINT NOT NULL DEFAULT 0,
    rule_name           VARCHAR(100) NOT NULL,
    rule_code           VARCHAR(50) NOT NULL,
    rule_type           VARCHAR(50) NOT NULL,
    rule_config         JSONB NOT NULL,
    enabled             BOOLEAN DEFAULT TRUE,
    description         VARCHAR(500),
    priority            INTEGER DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT uk_site_rule_code UNIQUE (site_id, rule_code)
);

-- 索引
CREATE INDEX idx_rule_site_id ON business_rule(site_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_rule_type ON business_rule(rule_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_rule_enabled ON business_rule(enabled) WHERE deleted_at IS NULL;

-- 表注释
COMMENT ON TABLE business_rule IS '业务规则表 - 存储状态转换、标准工时、告警阈值等规则';
COMMENT ON COLUMN business_rule.site_id IS '站点ID';
COMMENT ON COLUMN business_rule.rule_name IS '规则名称';
COMMENT ON COLUMN business_rule.rule_code IS '规则编码';
COMMENT ON COLUMN business_rule.rule_type IS '规则类型: STATE_TRANSITION, PDI_STANDARD_TIME, ALARM_THRESHOLD';
COMMENT ON COLUMN business_rule.rule_config IS '规则配置JSON';
COMMENT ON COLUMN business_rule.enabled IS '是否启用';
COMMENT ON COLUMN business_rule.description IS '规则描述';
COMMENT ON COLUMN business_rule.priority IS '优先级，数值越大优先级越高';

-- -----------------------------------------------------
-- Table: system_config
-- 系统配置表 - 存储通用系统配置项
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS system_config (
    id                  BIGSERIAL PRIMARY KEY,
    site_id             BIGINT NOT NULL DEFAULT 0,
    config_key          VARCHAR(100) NOT NULL,
    config_value        TEXT,
    config_group        VARCHAR(50) NOT NULL,
    description         VARCHAR(500),
    value_type          VARCHAR(20) DEFAULT 'STRING',
    default_value       TEXT,
    editable            BOOLEAN DEFAULT TRUE,
    builtin             BOOLEAN DEFAULT FALSE,
    sort_order          INTEGER DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT uk_site_config_key UNIQUE (site_id, config_key)
);

-- 索引
CREATE INDEX idx_config_group ON system_config(config_group) WHERE deleted_at IS NULL;
CREATE INDEX idx_config_site_id ON system_config(site_id) WHERE deleted_at IS NULL;

-- 表注释
COMMENT ON TABLE system_config IS '系统配置表 - 存储通用系统配置项';
COMMENT ON COLUMN system_config.site_id IS '站点ID';
COMMENT ON COLUMN system_config.config_key IS '配置键';
COMMENT ON COLUMN system_config.config_value IS '配置值';
COMMENT ON COLUMN system_config.config_group IS '配置分组';
COMMENT ON COLUMN system_config.description IS '配置描述';
COMMENT ON COLUMN system_config.value_type IS '值类型: STRING, INTEGER, BOOLEAN, JSON';
COMMENT ON COLUMN system_config.default_value IS '默认值';
COMMENT ON COLUMN system_config.editable IS '是否可编辑';
COMMENT ON COLUMN system_config.builtin IS '是否系统内置';
COMMENT ON COLUMN system_config.sort_order IS '排序顺序';

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
}'::jsonb, TRUE, 'PDI作业默认状态转换规则', 0);

-- 默认PDI标准工时配置
INSERT INTO business_rule (site_id, rule_name, rule_code, rule_type, rule_config, enabled, description, priority) VALUES
(0, '默认PDI标准工时配置', 'DEFAULT_PDI_TIME', 'PDI_STANDARD_TIME', '{
    "defaultStandardDuration": 300,
    "overtimeThresholdPercent": 120,
    "channelConfigs": []
}'::jsonb, TRUE, 'PDI作业默认标准工时配置', 0);

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
}'::jsonb, TRUE, '告警级别阈值默认配置', 0);

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
