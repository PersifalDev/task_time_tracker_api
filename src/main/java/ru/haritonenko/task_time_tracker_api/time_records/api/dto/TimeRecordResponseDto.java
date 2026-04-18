package ru.haritonenko.task_time_tracker_api.time_records.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimeRecordResponseDto(
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