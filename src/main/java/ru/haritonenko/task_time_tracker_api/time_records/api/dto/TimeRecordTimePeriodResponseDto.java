package ru.haritonenko.task_time_tracker_api.time_records.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimeRecordTimePeriodResponseDto(
        Long employeeId,
        Long totalTasksCount,
        Long totalDoneTasksCount,
        Long totalSpentMinutes,
        OffsetDateTime firstTimeBoundPeriod,
        OffsetDateTime secondTimeBoundPeriod
) {
}
