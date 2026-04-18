package ru.haritonenko.task_time_tracker_api.time_records.domain.mapper;

import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.time_records.domain.TimeRecord;
import ru.haritonenko.task_time_tracker_api.time_records.domain.db.entity.TimeRecordEntity;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:47:41+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class TimeRecordToDomainMapperImpl implements TimeRecordToDomainMapper {

    @Override
    public TimeRecord toDomain(TimeRecordEntity timeRecordEntity) {
        if ( timeRecordEntity == null ) {
            return null;
        }

        Long id = null;
        Long employeeId = null;
        Long taskId = null;
        String description = null;
        OffsetDateTime startTime = null;
        OffsetDateTime endTime = null;
        OffsetDateTime createdAt = null;
        OffsetDateTime updatedAt = null;

        id = timeRecordEntity.getId();
        employeeId = timeRecordEntity.getEmployeeId();
        taskId = timeRecordEntity.getTaskId();
        description = timeRecordEntity.getDescription();
        startTime = timeRecordEntity.getStartTime();
        endTime = timeRecordEntity.getEndTime();
        createdAt = timeRecordEntity.getCreatedAt();
        updatedAt = timeRecordEntity.getUpdatedAt();

        TimeRecord timeRecord = new TimeRecord( id, employeeId, taskId, description, startTime, endTime, createdAt, updatedAt );

        return timeRecord;
    }
}
