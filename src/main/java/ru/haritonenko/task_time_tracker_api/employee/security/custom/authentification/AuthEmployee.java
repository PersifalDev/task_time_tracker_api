package ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AuthEmployee(
        @NotNull(message = "Employee id can not be null")
        Long id,
        @NotBlank(message = "Employee login can not be blank")
        String login,
        @NotBlank(message = "Employee role can not be blank")
        String role
) {
}