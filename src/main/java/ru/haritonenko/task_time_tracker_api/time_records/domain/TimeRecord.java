package ru.haritonenko.task_time_tracker_api.time_records.domain;

import java.time.OffsetDateTime;

public record TimeRecord(
        Long id,
        Long employeeId,
        Long taskId,
        String description,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}