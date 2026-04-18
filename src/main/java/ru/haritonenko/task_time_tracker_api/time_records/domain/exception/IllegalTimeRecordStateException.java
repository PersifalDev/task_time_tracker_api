package ru.haritonenko.task_time_tracker_api.time_records.domain.exception;

public class IllegalTimeRecordStateException extends RuntimeException {
    public IllegalTimeRecordStateException(String message) {
        super(message);
    }
}
