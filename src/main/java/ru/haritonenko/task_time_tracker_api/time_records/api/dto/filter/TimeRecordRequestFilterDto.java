package ru.haritonenko.task_time_tracker_api.time_records.api.dto.filter;

import jakarta.validation.constraints.NotNull;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.custom.annotation.ValidTimePeriod;

import java.time.OffsetDateTime;

@ValidTimePeriod
public record TimeRecordRequestFilterDto(
        @NotNull(message = "Employee id must not be null")
        Long employeeId,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}