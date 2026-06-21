package ru.haritonenko.task_time_tracker_api.tasks.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.haritonenko.task_time_tracker_api.tasks.domain.priority.TaskPriority;

public record TaskCreateRequestDto(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,
        @NotBlank(message = "Description must not be blank")
        String description,
        @NotNull(message = "Priority must not be blank")
        TaskPriority priority

) {
}