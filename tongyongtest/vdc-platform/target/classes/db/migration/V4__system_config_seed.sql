-- Seed data for system_config (thresholds and general configs)

INSERT INTO system_config (config_key, config_value, description) VALUES
('THRESHOLD_DURATION_WARNING', '90', '标准工时预警阈值(%)'),
('THRESHOLD_PERSON_ABSENT', '10', '人员消失超时(秒)'),
('SYSTEM_NAME', '金桥基地VDC业务平台', '系统名称'),
('LOGO_URL', '/logo.png', 'Logo地址'),
('ALARM_AUTO_CLEAR', '72', '预警自动清除(小时)'),
('REPORT_RETENTION_DAYS', '90', '报表保留天数')
ON CONFLICT (config_key) DO NOTHING;
