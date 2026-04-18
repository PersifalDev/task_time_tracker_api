package ru.haritonenko.task_time_tracker_api.time_records.domain.db.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TimeRecordEntity {
    private Long id;
    @NotNull(message = "Employee id must not be null")
    private Long employeeId;
    @NotNull(message = "Task id must not be null")
    private Long taskId;
    @NotBlank(message = "Description must not be blank")
    private String description;
    @NotNull(message = "Start time must not be null")
    private OffsetDateTime startTime;
    @NotNull(message = "End time must not be null")
    private OffsetDateTime endTime;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}