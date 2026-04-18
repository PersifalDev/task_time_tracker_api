package ru.haritonenko.task_time_tracker_api.tasks.api.dto;

import jakarta.validation.constraints.NotNull;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

public record TaskUpdateRequestDto(
        @NotNull(message = "Status must not be null")
        TaskStatus status
) {
}