package ru.haritonenko.task_time_tracker_api.time_records.domain;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record TimeRecordInfo(
        Long employeeId,
        Long totalTasksCount,
        Long totalDoneTasksCount,
        Long totalSpentMinutes,
        OffsetDateTime firstTimeBoundPeriod,
        OffsetDateTime secondTimeBoundPeriod
) {
}
