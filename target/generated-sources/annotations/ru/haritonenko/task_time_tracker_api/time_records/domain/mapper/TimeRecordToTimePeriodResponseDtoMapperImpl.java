package ru.haritonenko.task_time_tracker_api.time_records.domain.mapper;

import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordTimePeriodResponseDto;
import ru.haritonenko.task_time_tracker_api.time_records.domain.TimeRecordInfo;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:47:40+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class TimeRecordToTimePeriodResponseDtoMapperImpl implements TimeRecordToTimePeriodResponseDtoMapper {

    @Override
    public TimeRecordTimePeriodResponseDto toResponseDto(TimeRecordInfo timeRecordInfo) {
        if ( timeRecordInfo == null ) {
            return null;
        }

        Long employeeId = null;
        Long totalTasksCount = null;
        Long totalDoneTasksCount = null;
        Long totalSpentMinutes = null;
        OffsetDateTime firstTimeBoundPeriod = null;
        OffsetDateTime secondTimeBoundPeriod = null;

        employeeId = timeRecordInfo.employeeId();
        totalTasksCount = timeRecordInfo.totalTasksCount();
        totalDoneTasksCount = timeRecordInfo.totalDoneTasksCount();
        totalSpentMinutes = timeRecordInfo.totalSpentMinutes();
        firstTimeBoundPeriod = timeRecordInfo.firstTimeBoundPeriod();
        secondTimeBoundPeriod = timeRecordInfo.secondTimeBoundPeriod();

        TimeRecordTimePeriodResponseDto timeRecordTimePeriodResponseDto = new TimeRecordTimePeriodResponseDto( employeeId, totalTasksCount, totalDoneTasksCount, totalSpentMinutes, firstTimeBoundPeriod, secondTimeBoundPeriod );

        return timeRecordTimePeriodResponseDto;
    }
}
