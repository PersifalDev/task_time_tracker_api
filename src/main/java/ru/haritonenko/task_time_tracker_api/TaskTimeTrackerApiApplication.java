package ru.haritonenko.task_time_tracker_api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.haritonenko.task_time_tracker_api.config.PageConfig;
import ru.haritonenko.task_time_tracker_api.config.properties.CacheProperties;

@MapperScan({
        "ru.haritonenko.task_time_tracker_api.tasks.domain.db.mapper",
        "ru.haritonenko.task_time_tracker_api.time_records.domain.db.mapper",
        "ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper"
})
@SpringBootApplication
@EnableConfigurationProperties({PageConfig.class, CacheProperties.class})
public class TaskTimeTrackerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskTimeTrackerApiApplication.class, args);
    }

}
