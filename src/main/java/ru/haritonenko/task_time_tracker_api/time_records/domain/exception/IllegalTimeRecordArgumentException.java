package ru.haritonenko.task_time_tracker_api.time_records.domain.exception;

public class IllegalTimeRecordArgumentException extends IllegalArgumentException {
    public IllegalTimeRecordArgumentException(String message) {
        super(message);
    }
}
