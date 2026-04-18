package ru.haritonenko.task_time_tracker_api.tasks.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskResponseDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}