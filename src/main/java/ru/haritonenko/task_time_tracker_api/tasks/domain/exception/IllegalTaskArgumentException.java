package ru.haritonenko.task_time_tracker_api.tasks.domain.exception;

public class IllegalTaskArgumentException extends IllegalArgumentException {
    public IllegalTaskArgumentException(String message) {
        super(message);
    }
}
