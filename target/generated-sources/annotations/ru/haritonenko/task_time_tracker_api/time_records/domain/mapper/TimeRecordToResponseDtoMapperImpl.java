package ru.haritonenko.task_time_tracker_api.time_records.domain.mapper;

import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordResponseDto;
import ru.haritonenko.task_time_tracker_api.time_records.domain.TimeRecord;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:47:41+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class TimeRecordToResponseDtoMapperImpl implements TimeRecordToResponseDtoMapper {

    @Override
    public TimeRecordResponseDto toResponseDto(TimeRecord timeRecord) {
        if ( timeRecord == null ) {
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

        id = timeRecord.id();
        employeeId = timeRecord.employeeId();
        taskId = timeRecord.taskId();
        description = timeRecord.description();
        startTime = timeRecord.startTime();
        endTime = timeRecord.endTime();
        createdAt = timeRecord.createdAt();
        updatedAt = timeRecord.updatedAt();

        TimeRecordResponseDto timeRecordResponseDto = new TimeRecordResponseDto( id, employeeId, taskId, description, startTime, endTime, createdAt, updatedAt );

        return timeRecordResponseDto;
    }
}
