package ru.haritonenko.task_time_tracker_api.tasks.domain.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
