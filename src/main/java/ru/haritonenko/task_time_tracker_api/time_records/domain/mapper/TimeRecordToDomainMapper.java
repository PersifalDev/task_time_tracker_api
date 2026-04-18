package ru.haritonenko.task_time_tracker_api.time_records.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.task_time_tracker_api.time_records.domain.TimeRecord;
import ru.haritonenko.task_time_tracker_api.time_records.domain.db.entity.TimeRecordEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TimeRecordToDomainMapper {

    TimeRecord toDomain(TimeRecordEntity timeRecordEntity);

}
