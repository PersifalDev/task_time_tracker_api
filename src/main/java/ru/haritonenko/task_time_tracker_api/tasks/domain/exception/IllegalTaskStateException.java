package ru.haritonenko.task_time_tracker_api.tasks.domain.exception;

public class IllegalTaskStateException extends RuntimeException {
    public IllegalTaskStateException(String message) {
        super(message);
    }
}
