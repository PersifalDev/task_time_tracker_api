package ru.haritonenko.task_time_tracker_api.employee.domain.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
