ALTER TABLE tasks
    ADD COLUMN priority VARCHAR(30);

UPDATE tasks
SET priority = 'MEDIUM'
WHERE priority IS NULL;

ALTER TABLE tasks
    ALTER COLUMN priority SET NOT NULL;
