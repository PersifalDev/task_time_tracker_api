package ru.haritonenko.task_time_tracker_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.pages")
public record PageConfig(
        Integer defaultPageNumber,
        Integer defaultPageSize
) {
}