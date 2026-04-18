package ru.haritonenko.task_time_tracker_api.tasks.domain.db.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

import java.time.OffsetDateTime;

@Setter
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    private Long id;
    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;
    @NotBlank(message = "Description must not be blank")
    private String description;
    @NotNull(message = "Status must not be null")
    private TaskStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}