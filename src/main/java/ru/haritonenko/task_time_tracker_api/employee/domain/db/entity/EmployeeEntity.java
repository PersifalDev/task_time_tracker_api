package ru.haritonenko.task_time_tracker_api.employee.domain.db.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;

import java.time.OffsetDateTime;

@Setter
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {

    private Long id;
    @NotBlank(message = "Employee login can not be blank")
    @Size(min = 4, max = 50, message = "Min login size is 4, max is 50")
    private String login;
    @NotBlank(message = "Employee password can not be blank")
    @Size(min = 4, max = 50, message = "Min password size is 4, max is 50")
    private String password;
    @NotNull(message = "Employee age can not be null")
    @Min(value = 18, message = "Min age value is 18")
    private Integer age;
    @NotNull(message = "Employee role can not be null")
    private EmployeeRole employeeRole;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}