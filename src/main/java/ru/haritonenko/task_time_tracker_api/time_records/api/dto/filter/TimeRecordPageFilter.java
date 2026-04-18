package ru.haritonenko.task_time_tracker_api.time_records.api.dto.filter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TimeRecordPageFilter(
        @Min(value = 0, message = "Page number must be greater than or equal to 0")
        Integer pageNumber,
        @Min(value = 1, message = "Page size must be greater than or equal to 1")
        @Max(value = 100, message = "Page size must be less than or equal to 100")
        Integer pageSize
) {

}