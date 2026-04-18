package ru.haritonenko.task_time_tracker_api.employee.domain.exception;

public class IllegalEmployeeStateException extends RuntimeException {
    public IllegalEmployeeStateException(String message) {
        super(message);
    }
}
