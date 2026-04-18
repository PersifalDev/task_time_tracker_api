package ru.haritonenko.task_time_tracker_api.time_records.domain.exception;

public class TimeRecordNotFoundException extends RuntimeException {
    public TimeRecordNotFoundException(String message) {
        super(message);
    }
}
