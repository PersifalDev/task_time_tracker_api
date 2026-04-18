package ru.haritonenko.task_time_tracker_api.employee.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.task_time_tracker_api.employee.domain.Employee;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmployeeToDomainMapper {

    @Mapping(source = "employeeRole", target = "role")
    Employee toDomain(EmployeeEntity employeeEntity);
}