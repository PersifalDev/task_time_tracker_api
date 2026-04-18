package ru.haritonenko.task_time_tracker_api.handler.error;

public record ErrorMessageResponse(
        String message,
        String detailedMessage,
        String dateTime
) {
}
