package ru.haritonenko.task_time_tracker_api.employee.api.dto.registration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeRegistration(
        @NotBlank(message = "Employee login can not be blank")
        @Size(min = 4, max = 50, message = "Min login size is 4, max is 50")
        String login,
        @NotBlank(message = "Employee password can not be blank")
        @Size(min = 4, max = 50, message = "Min password size is 4, max is 50")
        String password,
        @NotNull(message = "Employee age can not be null")
        @Min(value = 18, message = "Min age value is 18")
        Integer age
) {
}