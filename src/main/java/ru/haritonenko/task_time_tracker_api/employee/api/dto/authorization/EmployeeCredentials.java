package ru.haritonenko.task_time_tracker_api.employee.api.dto.authorization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeCredentials(
        @NotBlank(message = "Employee login can not be blank")
        @Size(min = 4, max = 50, message = "Min login size is 4, max is 50")
        String login,
        @NotBlank(message = "Employee password can not be blank")
        @Size(min = 4, max = 50, message = "Min password size is 4, max is 50")
        String password
) {
}