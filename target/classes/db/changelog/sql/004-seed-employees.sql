INSERT INTO employees (id, login, password, age, employee_role, created_at, updated_at)
SELECT 3, 'employee3', '$2a$10$3euPcmQFCiblsZeEu5s7p.e/QD8Yg9xTjM1Myl6Nh11s2x7fOe5y.', 23, 'USER', '2026-04-01T08:00:00Z', '2026-04-01T08:00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE id = 3 OR login = 'employee3');

INSERT INTO employees (id, login, password, age, employee_role, created_at, updated_at)
SELECT 4, 'employee4', '$2a$10$7EqJtq98hPqEX7fNZaFWo.8k4sNQH6I4S4v8YVjM5vQeFf8zN1G2u', 24, 'USER', '2026-04-01T08:05:00Z', '2026-04-01T08:05:00Z'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE id = 4 OR login = 'employee4');

INSERT INTO employees (id, login, password, age, employee_role, created_at, updated_at)
SELECT 5, 'employee5', '$2a$10$7EqJtq98hPqEX7fNZaFWoOzvL6R0qK8Q1xQnJ5lWmYJm7lX9mJ3mK', 25, 'USER', '2026-04-01T08:10:00Z', '2026-04-01T08:10:00Z'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE id = 5 OR login = 'employee5');

INSERT INTO employees (id, login, password, age, employee_role, created_at, updated_at)
SELECT 6, 'employee6', '$2a$10$7EqJtq98hPqEX7fNZaFWoOc4d7Y6nB2lR4fJ8nP0qT6xL2mK9jWmS', 26, 'USER', '2026-04-01T08:15:00Z', '2026-04-01T08:15:00Z'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE id = 6 OR login = 'employee6');

INSERT INTO employees (id, login, password, age, employee_role, created_at, updated_at)
SELECT 7, 'employee7', '$2a$10$7EqJtq98hPqEX7fNZaFWoOLhQm7vO0cL4pM3sT9wX1yZ8nK5rP2bC', 27, 'USER', '2026-04-01T08:20:00Z', '2026-04-01T08:20:00Z'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE id = 7 OR login = 'employee7');

SELECT setval(pg_get_serial_sequence('employees', 'id'), COALESCE((SELECT MAX(id) FROM employees), 1), true);