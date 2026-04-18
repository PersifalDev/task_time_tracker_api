package ru.haritonenko.task_time_tracker_api.employee.domain.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.response.EmployeeDtoResponse;
import ru.haritonenko.task_time_tracker_api.employee.domain.Employee;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:47:41+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class EmployeeToDtoResponseMapperImpl implements EmployeeToDtoResponseMapper {

    @Override
    public EmployeeDtoResponse toDto(Employee employee) {
        if ( employee == null ) {
            return null;
        }

        Long id = null;
        String login = null;

        id = employee.id();
        login = employee.login();

        EmployeeDtoResponse employeeDtoResponse = new EmployeeDtoResponse( id, login );

        return employeeDtoResponse;
    }
}
