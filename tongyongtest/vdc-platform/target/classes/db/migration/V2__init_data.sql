-- Initial data for VDC Platform

-- sites
INSERT INTO site (site_code, site_name) VALUES
('JINQIAO', '金桥库'),
('KAIDI', '凯迪库')
ON CONFLICT (site_code) DO NOTHING;

-- roles
INSERT INTO sys_role (role_code, role_name, permissions, data_scope) VALUES
('SUPER_ADMIN', '超级管理员', '["*"]', 'ALL'),
('SITE_ADMIN', '站点管理员', '["alarm:read","alarm:process","report:read","report:export","device:read","device:write","config:read","config:write"]', 'SITE_SPECIFIC'),
('READONLY', '只读用户', '["alarm:read","report:read","device:read","config:read"]', 'SITE_SPECIFIC')
ON CONFLICT (role_code) DO NOTHING;

-- admin user (password is BCrypt hash of 'admin123')
INSERT INTO sys_user (username, password_hash, real_name, phone, email, role_id, site_id, status)
SELECT 'admin', '$2a$10$fsM3bRQAXPQC0fRG/r231.l68g006czb66MkXskYyuQ4maxuLFJAG', '系统管理员', '13800138000', 'admin@vdc.com', sr.id, NULL, 1
FROM sys_role sr WHERE sr.role_code = 'SUPER_ADMIN'
ON CONFLICT (username) DO NOTHING;

-- rule configs (standard durations from appendix 10.2)
INSERT INTO rule_config (rule_name, channel_type, require_vehicle, enter_pattern, exit_pattern, standard_duration, critical_threshold_pct, person_absent_timeout, is_enabled) VALUES
('左前门PDI规则', 'PDI_FRONT', true, '[15,16,11]', '[11,16,15,[13,9]]', 720, 90.00, 10, true),
('左后门PDI规则', 'PDI_REAR', true, '[15,16,11]', '[11,16,15,[13,9]]', 180, 90.00, 10, true),
('滑移门PDI规则', 'PDI_SLIDING', true, '[15,16,11]', '[11,16,15,[13,9]]', 480, 90.00, 10, true)
ON CONFLICT DO NOTHING;
