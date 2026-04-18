package ru.haritonenko.task_time_tracker_api.employee.domain.exception;

public class IllegalEmployeeArgumentException extends RuntimeException {
    public IllegalEmployeeArgumentException(String message) {
        super(message);
    }
}
