package ru.haritonenko.task_time_tracker_api.tasks.domain;

import ru.haritonenko.task_time_tracker_api.tasks.domain.priority.TaskPriority;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

import java.time.OffsetDateTime;

public record Task(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
