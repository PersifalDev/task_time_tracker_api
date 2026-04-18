INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 101, 'Prepare API docs', 'Prepare OpenAPI description for public endpoints', 'DONE', '2026-04-02T09:00:00Z', '2026-04-02T12:00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 101);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 102, 'Implement auth filter', 'Add JWT authentication filter for protected endpoints', 'IN_PROGRESS', '2026-04-03T09:00:00Z', '2026-04-03T12:45:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 102);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 103, 'Create task mapper', 'Implement MapStruct mapper for task responses', 'DONE', '2026-04-04T09:00:00Z', '2026-04-04T13:30:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 103);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 104, 'Validate time period', 'Add validation for time record request period', 'NEW', '2026-04-05T09:00:00Z', '2026-04-05T11:30:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 104);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 105, 'Fix info endpoint', 'Fix aggregate SQL for employee time statistics', 'DONE', '2026-04-06T09:00:00Z', '2026-04-06T16:15:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 105);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 106, 'Add search endpoint', 'Implement filtered time record search', 'DONE', '2026-04-07T09:00:00Z', '2026-04-07T11:15:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 106);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 107, 'Implement security rules', 'Configure role access for endpoints', 'DONE', '2026-04-08T09:00:00Z', '2026-04-08T12:40:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 107);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 108, 'Implement employee service', 'Add registration and lookup logic', 'IN_PROGRESS', '2026-04-09T09:00:00Z', '2026-04-09T12:10:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 108);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 109, 'Create integration tests', 'Add database integration test coverage', 'DONE', '2026-04-10T09:00:00Z', '2026-04-10T15:20:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 109);

INSERT INTO tasks (id, title, description, status, created_at, updated_at)
SELECT 110, 'Seed database', 'Prepare Liquibase seed data for manual checks', 'DONE', '2026-04-11T09:00:00Z', '2026-04-11T10:20:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 110);

SELECT setval(pg_get_serial_sequence('tasks', 'id'), COALESCE((SELECT MAX(id) FROM tasks), 1), true);