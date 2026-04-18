package ru.haritonenko.task_time_tracker_api.time_records.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.custom.annotation.ValidTimePeriod;

import java.time.OffsetDateTime;

@ValidTimePeriod
public record TimeRecordCreateRequestDto(
        @NotNull(message = "Employee id must not be null")
        Long employeeId,
        @NotNull(message = "Task id must not be null")
        Long taskId,
        @NotBlank(message = "Description must not be blank")
        String description,
        @NotNull(message = "Start time must not be null")
        OffsetDateTime startTime,
        @NotNull(message = "End time must not be null")
        OffsetDateTime endTime
) {
}