INSERT INTO tasks (id, title, description, status, priority, created_at, updated_at)
SELECT 111, 'Review priority filter', 'Check task search by HIGH priority and NEW status', 'NEW', 'HIGH', '2026-04-12T09:00:00Z', '2026-04-12T09:00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 111);

INSERT INTO tasks (id, title, description, status, priority, created_at, updated_at)
SELECT 112, 'Verify low priority task', 'Check task search by LOW priority and DONE status', 'DONE', 'LOW', '2026-04-12T10:00:00Z', '2026-04-12T10:00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 112);

SELECT setval(pg_get_serial_sequence('tasks', 'id'), COALESCE((SELECT MAX(id) FROM tasks), 1), true);
