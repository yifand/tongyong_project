-- 测试数据初始化脚本
-- 用于Repository层测试和集成测试

-- 清理数据（如果需要）
DELETE FROM archive_timeline WHERE archive_id IN (1001, 1002, 1003, 1004);
DELETE FROM behavior_archive WHERE id IN (1001, 1002, 1003, 1004);
DELETE FROM pdi_task WHERE id IN (2001, 2002, 2003, 2004);

-- 插入PDI任务数据
INSERT INTO pdi_task (id, channel_id, site_id, start_time, end_time, estimated_duration, actual_duration, result, created_at, updated_at)
VALUES
    (2001, 101, 1, '2026-03-25 10:00:00', '2026-03-25 10:08:32', 10, 512, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2002, 102, 1, '2026-03-25 09:30:15', '2026-03-25 09:35:20', 15, 305, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2003, 103, 2, '2026-03-25 09:15:00', NULL, 12, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2004, 104, 1, '2026-03-25 08:00:00', '2026-03-25 08:12:00', 10, 720, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入行为档案数据
INSERT INTO behavior_archive (id, pdi_task_id, channel_id, site_id, start_time, end_time, estimated_duration, actual_duration, status, created_at, updated_at)
VALUES
    (1001, 2001, 101, 1, '2026-03-25 10:00:00', '2026-03-25 10:08:32', 10, 512, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1002, 2002, 102, 1, '2026-03-25 09:30:15', '2026-03-25 09:35:20', 15, 305, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1003, 2003, 103, 2, '2026-03-25 09:15:00', NULL, 12, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1004, 2004, 104, 1, '2026-03-25 08:00:00', '2026-03-25 08:12:00', 10, 720, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入时间线数据
INSERT INTO archive_timeline (id, archive_id, event_time, action, image_url, seq, created_at, updated_at)
VALUES
    (1, 1001, '2026-03-25 10:00:00', '人员进入车内', 'http://minio/archives/2026-03-25/enter_100001.jpg', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1001, '2026-03-25 10:02:15', '开始检查作业', 'http://minio/archives/2026-03-25/check_100002.jpg', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1001, '2026-03-25 10:05:30', '检查进行中', 'http://minio/archives/2026-03-25/process_100003.jpg', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 1001, '2026-03-25 10:08:32', '人员离开，检查结束', 'http://minio/archives/2026-03-25/exit_100004.jpg', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 1002, '2026-03-25 09:30:15', '人员进入车内', 'http://minio/archives/2026-03-25/enter_100005.jpg', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 1002, '2026-03-25 09:35:20', '人员离开，检查结束', 'http://minio/archives/2026-03-25/exit_100006.jpg', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 1003, '2026-03-25 09:15:00', '人员进入车内', 'http://minio/archives/2026-03-25/enter_100007.jpg', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
