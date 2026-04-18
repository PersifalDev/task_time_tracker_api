package ru.haritonenko.task_time_tracker_api.employee.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.response.EmployeeDtoResponse;
import ru.haritonenko.task_time_tracker_api.employee.domain.Employee;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmployeeToDtoResponseMapper {

    EmployeeDtoResponse toDto(Employee employee);
}