package ru.haritonenko.task_time_tracker_api.tasks.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.TaskResponseDto;
import ru.haritonenko.task_time_tracker_api.tasks.domain.Task;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TaskToResponseDtoMapper {

    TaskResponseDto toResponseDto(Task task);
}