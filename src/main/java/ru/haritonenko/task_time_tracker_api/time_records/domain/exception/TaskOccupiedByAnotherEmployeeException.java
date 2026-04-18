package ru.haritonenko.task_time_tracker_api.time_records.domain.exception;

public class TaskOccupiedByAnotherEmployeeException extends RuntimeException {
    public TaskOccupiedByAnotherEmployeeException(String message) {
        super(message);
    }
}
