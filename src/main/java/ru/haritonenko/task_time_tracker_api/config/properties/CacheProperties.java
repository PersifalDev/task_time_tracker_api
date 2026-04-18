package ru.haritonenko.task_time_tracker_api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(
        Duration defaultTtl,
        Duration tasksTtl,
        Duration employeesTtl,
        Duration timeRecordsTtl
) {
}
