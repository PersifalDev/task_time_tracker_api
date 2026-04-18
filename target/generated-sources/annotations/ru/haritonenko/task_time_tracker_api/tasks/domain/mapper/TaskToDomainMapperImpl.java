package ru.haritonenko.task_time_tracker_api.tasks.domain.mapper;

import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.tasks.domain.Task;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.entity.TaskEntity;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:47:41+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class TaskToDomainMapperImpl implements TaskToDomainMapper {

    @Override
    public Task toDomain(TaskEntity taskEntity) {
        if ( taskEntity == null ) {
            return null;
        }

        Long id = null;
        String title = null;
        String description = null;
        TaskStatus status = null;
        OffsetDateTime createdAt = null;
        OffsetDateTime updatedAt = null;

        id = taskEntity.getId();
        title = taskEntity.getTitle();
        description = taskEntity.getDescription();
        status = taskEntity.getStatus();
        createdAt = taskEntity.getCreatedAt();
        updatedAt = taskEntity.getUpdatedAt();

        Task task = new Task( id, title, description, status, createdAt, updatedAt );

        return task;
    }
}
