package ru.haritonenko.task_time_tracker_api.time_records.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordResponseDto;
import ru.haritonenko.task_time_tracker_api.time_records.domain.TimeRecord;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TimeRecordToResponseDtoMapper {

    TimeRecordResponseDto toResponseDto(TimeRecord timeRecord);
}