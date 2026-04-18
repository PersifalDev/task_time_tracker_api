package ru.haritonenko.task_time_tracker_api.employee.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.registration.EmployeeRegistration;
import ru.haritonenko.task_time_tracker_api.employee.domain.Employee;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.EmployeeNotFoundException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeArgumentException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeStateException;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;
import ru.haritonenko.task_time_tracker_api.integration.AbstractIntegrationTest;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeEntityMapper employeeEntityMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Test
    void shouldSuccessfullyGetEmployeeById() {
        EmployeeEntity savedEmployeeEntity = saveDummyEmployee("employee_get_by_id");

        Employee foundEmployee = employeeService.getEmployeeById(savedEmployeeEntity.getId());

        assertEquals(savedEmployeeEntity.getId(), foundEmployee.id());
        assertEquals(savedEmployeeEntity.getLogin(), foundEmployee.login());
        assertEquals(savedEmployeeEntity.getAge(), foundEmployee.age());
        assertEquals(savedEmployeeEntity.getEmployeeRole(), foundEmployee.role());
    }

    @Transactional
    @Test
    void shouldSuccessfullyRegisterEmployee() {
        EmployeeRegistration employeeRegistration = new EmployeeRegistration(
                "employee_register_test",
                "password123",
                22
        );

        Employee registeredEmployee = employeeService.register(employeeRegistration);

        assertNotNull(registeredEmployee.id());
        assertEquals(employeeRegistration.login(), registeredEmployee.login());
        assertEquals(employeeRegistration.age(), registeredEmployee.age());
        assertEquals(EmployeeRole.USER, registeredEmployee.role());

        EmployeeEntity foundEmployeeEntity = employeeEntityMapper.findById(registeredEmployee.id()).orElseThrow();
        assertEquals(employeeRegistration.login(), foundEmployeeEntity.getLogin());
        assertTrue(passwordEncoder.matches(employeeRegistration.password(), foundEmployeeEntity.getPassword()));
    }

    @Transactional
    @Test
    void shouldSuccessfullyFindEmployeeByLogin() {
        EmployeeEntity savedEmployeeEntity = saveDummyEmployee("employee_find_login");

        Employee foundEmployee = employeeService.findByLogin(savedEmployeeEntity.getLogin());

        assertEquals(savedEmployeeEntity.getId(), foundEmployee.id());
        assertEquals(savedEmployeeEntity.getLogin(), foundEmployee.login());
        assertEquals(savedEmployeeEntity.getAge(), foundEmployee.age());
        assertEquals(savedEmployeeEntity.getEmployeeRole(), foundEmployee.role());
    }

    @Transactional
    @Test
    void shouldThrowIllegalEmployeeArgumentExceptionWhenEmployeeIdIsNull() {
        IllegalEmployeeArgumentException exception = assertThrows(
                IllegalEmployeeArgumentException.class,
                () -> employeeService.getEmployeeById(null)
        );

        assertEquals("Employee id is null", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowEmployeeNotFoundExceptionWhenEmployeeNotFoundById() {
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(Long.MAX_VALUE)
        );

        assertEquals("Employee with id=%d not found".formatted(Long.MAX_VALUE), exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalEmployeeArgumentExceptionWhenEmployeeRegistrationIsNull() {
        IllegalEmployeeArgumentException exception = assertThrows(
                IllegalEmployeeArgumentException.class,
                () -> employeeService.register(null)
        );

        assertEquals("Employee registration request is null", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalEmployeeStateExceptionWhenEmployeeAlreadyRegistered() {
        saveDummyEmployee("employee_already_exists");

        IllegalEmployeeStateException exception = assertThrows(
                IllegalEmployeeStateException.class,
                () -> employeeService.register(new EmployeeRegistration("employee_already_exists", "password123", 22))
        );

        assertEquals("Employee with login=employee_already_exists is already registered", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalEmployeeArgumentExceptionWhenLoginIsNullOrBlank() {
        IllegalEmployeeArgumentException exceptionForNull = assertThrows(
                IllegalEmployeeArgumentException.class,
                () -> employeeService.findByLogin(null)
        );
        assertEquals("Employee login is null or blank", exceptionForNull.getMessage());

        IllegalEmployeeArgumentException exceptionForBlank = assertThrows(
                IllegalEmployeeArgumentException.class,
                () -> employeeService.findByLogin(" ")
        );
        assertEquals("Employee login is null or blank", exceptionForBlank.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowEmployeeNotFoundExceptionWhenEmployeeNotFoundByLogin() {
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.findByLogin("missing-login")
        );

        assertEquals("Employee with login=missing-login not found", exception.getMessage());
    }

    private EmployeeEntity saveDummyEmployee(String login) {
        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .login(login)
                .password(passwordEncoder.encode("password123"))
                .age(21)
                .employeeRole(EmployeeRole.USER)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        employeeEntityMapper.insert(employeeEntity);
        return employeeEntity;
    }
}
