package ru.haritonenko.task_time_tracker_api.time_records.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordTimePeriodResponseDto;
import ru.haritonenko.task_time_tracker_api.time_records.domain.TimeRecordInfo;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TimeRecordToTimePeriodResponseDtoMapper {

    TimeRecordTimePeriodResponseDto toResponseDto(TimeRecordInfo timeRecordInfo);
}