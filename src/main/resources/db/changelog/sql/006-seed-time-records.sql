INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1001, 3, 101, 'Prepared swagger schema for tasks', '2026-04-02T10:00:00Z', '2026-04-02T12:00:00Z', '2026-04-02T12:00:00Z', '2026-04-02T12:00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1001);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1002, 3, 103, 'Finished response mapper for tasks', '2026-04-04T11:00:00Z', '2026-04-04T13:30:00Z', '2026-04-04T13:30:00Z', '2026-04-04T13:30:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1002);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1003, 3, 105, 'Reworked aggregate query for info endpoint', '2026-04-06T14:00:00Z', '2026-04-06T16:15:00Z', '2026-04-06T16:15:00Z', '2026-04-06T16:15:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1003);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1004, 4, 102, 'Started JWT filter implementation', '2026-04-03T10:00:00Z', '2026-04-03T12:45:00Z', '2026-04-03T12:45:00Z', '2026-04-03T12:45:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1004);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1005, 4, 104, 'Prepared validation annotations for time period', '2026-04-05T10:30:00Z', '2026-04-05T11:30:00Z', '2026-04-05T11:30:00Z', '2026-04-05T11:30:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1005);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1006, 4, 106, 'Implemented paged search for time records', '2026-04-07T09:15:00Z', '2026-04-07T11:15:00Z', '2026-04-07T11:15:00Z', '2026-04-07T11:15:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1006);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1007, 5, 107, 'Configured access rules for admin and user', '2026-04-08T09:40:00Z', '2026-04-08T12:40:00Z', '2026-04-08T12:40:00Z', '2026-04-08T12:40:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1007);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1008, 5, 108, 'Implemented employee registration checks', '2026-04-09T10:00:00Z', '2026-04-09T12:10:00Z', '2026-04-09T12:10:00Z', '2026-04-09T12:10:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1008);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1009, 6, 109, 'Wrote service integration tests', '2026-04-10T11:00:00Z', '2026-04-10T15:20:00Z', '2026-04-10T15:20:00Z', '2026-04-10T15:20:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1009);

INSERT INTO time_records (id, employee_id, task_id, description, start_time, end_time, created_at, updated_at)
SELECT 1010, 7, 110, 'Prepared seed data for manual verification', '2026-04-11T09:10:00Z', '2026-04-11T10:20:00Z', '2026-04-11T10:20:00Z', '2026-04-11T10:20:00Z'
WHERE NOT EXISTS (SELECT 1 FROM time_records WHERE id = 1010);

SELECT setval(pg_get_serial_sequence('time_records', 'id'), COALESCE((SELECT MAX(id) FROM time_records), 1), true);