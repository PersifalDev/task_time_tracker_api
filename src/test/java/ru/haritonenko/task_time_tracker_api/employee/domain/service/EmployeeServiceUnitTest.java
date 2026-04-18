package ru.haritonenko.task_time_tracker_api.employee.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.haritonenko.task_time_tracker_api.config.properties.CacheProperties;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.registration.EmployeeRegistration;
import ru.haritonenko.task_time_tracker_api.employee.domain.Employee;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.EmployeeNotFoundException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeArgumentException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeStateException;
import ru.haritonenko.task_time_tracker_api.employee.domain.mapper.EmployeeToDomainMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceUnitTest {

    @Mock
    private EmployeeEntityMapper employeeEntityMapper;
    @Mock
    private EmployeeToDomainMapper mapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ObjectProvider<RedisTemplate<String, Employee>> redisEmployeeTemplateProvider;
    @Mock
    private CacheProperties cacheProperties;

    private EmployeeService employeeService;

    private EmployeeEntity employeeEntity;
    private Employee employeeDomain;
    private EmployeeRegistration employeeRegistration;

    @BeforeEach
    void setUp() {
        when(redisEmployeeTemplateProvider.getIfAvailable()).thenReturn(null);

        employeeService = new EmployeeService(
                employeeEntityMapper,
                mapper,
                passwordEncoder,
                redisEmployeeTemplateProvider,
                cacheProperties
        );

        OffsetDateTime now = OffsetDateTime.parse("2026-04-16T10:00:00Z");
        employeeEntity = EmployeeEntity.builder()
                .id(1L)
                .login("employee")
                .password("encoded-password")
                .age(22)
                .employeeRole(EmployeeRole.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        employeeDomain = new Employee(1L, "employee", 22, EmployeeRole.USER);
        employeeRegistration = new EmployeeRegistration("employee", "password123", 22);
    }

    @Test
    void shouldSuccessfullyGetEmployeeById() {
        when(employeeEntityMapper.findById(1L)).thenReturn(Optional.of(employeeEntity));
        when(mapper.toDomain(employeeEntity)).thenReturn(employeeDomain);

        Employee foundEmployee = employeeService.getEmployeeById(1L);

        assertEquals(employeeDomain.id(), foundEmployee.id());
        assertEquals(employeeDomain.login(), foundEmployee.login());
        assertEquals(employeeDomain.age(), foundEmployee.age());
        assertEquals(employeeDomain.role(), foundEmployee.role());

        verify(employeeEntityMapper).findById(1L);
        verify(mapper).toDomain(employeeEntity);
        verify(redisEmployeeTemplateProvider).getIfAvailable();
    }

    @Test
    void shouldSuccessfullyRegisterEmployee() {
        doAnswer(invocation -> {
            EmployeeEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return null;
        }).when(employeeEntityMapper).insert(any(EmployeeEntity.class));

        when(employeeEntityMapper.existsByLogin("employee")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(mapper.toDomain(any(EmployeeEntity.class))).thenReturn(employeeDomain);

        Employee registeredEmployee = employeeService.register(employeeRegistration);

        assertNotNull(registeredEmployee.id());
        assertEquals("employee", registeredEmployee.login());
        assertEquals(22, registeredEmployee.age());
        assertEquals(EmployeeRole.USER, registeredEmployee.role());

        verify(employeeEntityMapper).existsByLogin("employee");
        verify(passwordEncoder).encode("password123");
        verify(employeeEntityMapper).insert(any(EmployeeEntity.class));
        verify(mapper).toDomain(any(EmployeeEntity.class));
    }

    @Test
    void shouldSuccessfullyFindEmployeeByLogin() {
        when(employeeEntityMapper.findByLogin("employee")).thenReturn(Optional.of(employeeEntity));
        when(mapper.toDomain(employeeEntity)).thenReturn(employeeDomain);

        Employee foundEmployee = employeeService.findByLogin("employee");

        assertEquals(employeeDomain.id(), foundEmployee.id());
        assertEquals(employeeDomain.login(), foundEmployee.login());
        assertEquals(employeeDomain.age(), foundEmployee.age());
        assertEquals(employeeDomain.role(), foundEmployee.role());

        verify(employeeEntityMapper).findByLogin("employee");
        verify(mapper).toDomain(employeeEntity);
    }

    @Test
    void shouldThrowIllegalEmployeeArgumentExceptionWhenEmployeeIdIsNull() {
        IllegalEmployeeArgumentException exception = assertThrows(
                IllegalEmployeeArgumentException.class,
                () -> employeeService.getEmployeeById(null)
        );

        assertEquals("Employee id is null", exception.getMessage());
    }

    @Test
    void shouldThrowEmployeeNotFoundExceptionWhenEmployeeNotFoundById() {
        when(employeeEntityMapper.findById(999L)).thenReturn(Optional.empty());

        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(999L)
        );

        assertEquals("Employee with id=999 not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalEmployeeArgumentExceptionWhenEmployeeRegistrationRequestIsNull() {
        IllegalEmployeeArgumentException exception = assertThrows(
                IllegalEmployeeArgumentException.class,
                () -> employeeService.register(null)
        );

        assertEquals("Employee registration request is null", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalEmployeeStateExceptionWhenEmployeeWithLoginAlreadyExists() {
        when(employeeEntityMapper.existsByLogin("employee")).thenReturn(true);

        IllegalEmployeeStateException exception = assertThrows(
                IllegalEmployeeStateException.class,
                () -> employeeService.register(employeeRegistration)
        );

        assertEquals("Employee with login=employee is already registered", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalEmployeeStateExceptionWhenEmployeeIdWasNotGeneratedAfterInsert() {
        when(employeeEntityMapper.existsByLogin("employee")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        IllegalEmployeeStateException exception = assertThrows(
                IllegalEmployeeStateException.class,
                () -> employeeService.register(employeeRegistration)
        );

        assertEquals("Employee id was not generated", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalEmployeeArgumentExceptionWhenEmployeeLoginIsNull() {
        IllegalEmployeeArgumentException exception = assertThrows(
                IllegalEmployeeArgumentException.class,
                () -> employeeService.findByLogin(null)
        );

        assertEquals("Employee login is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalEmployeeArgumentExceptionWhenEmployeeLoginIsBlank() {
        IllegalEmployeeArgumentException exception = assertThrows(
                IllegalEmployeeArgumentException.class,
                () -> employeeService.findByLogin(" ")
        );

        assertEquals("Employee login is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowEmployeeNotFoundExceptionWhenEmployeeNotFoundByLogin() {
        when(employeeEntityMapper.findByLogin("unknown")).thenReturn(Optional.empty());

        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.findByLogin("unknown")
        );

        assertEquals("Employee with login=unknown not found", exception.getMessage());
    }
}