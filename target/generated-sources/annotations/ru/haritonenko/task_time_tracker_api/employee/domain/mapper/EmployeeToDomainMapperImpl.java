package ru.haritonenko.task_time_tracker_api.employee.domain.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.employee.domain.Employee;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:47:41+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class EmployeeToDomainMapperImpl implements EmployeeToDomainMapper {

    @Override
    public Employee toDomain(EmployeeEntity employeeEntity) {
        if ( employeeEntity == null ) {
            return null;
        }

        EmployeeRole role = null;
        Long id = null;
        String login = null;
        Integer age = null;

        role = employeeEntity.getEmployeeRole();
        id = employeeEntity.getId();
        login = employeeEntity.getLogin();
        age = employeeEntity.getAge();

        Employee employee = new Employee( id, login, age, role );

        return employee;
    }
}
