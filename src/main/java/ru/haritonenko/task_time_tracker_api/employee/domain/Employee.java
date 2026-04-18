package ru.haritonenko.task_time_tracker_api.employee.domain;

import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;

public record Employee(
        Long id,
        String login,
        Integer age,
        EmployeeRole role
) {
}