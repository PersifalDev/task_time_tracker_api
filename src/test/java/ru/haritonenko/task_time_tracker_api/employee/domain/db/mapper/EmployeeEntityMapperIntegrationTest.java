package ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import ru.haritonenko.task_time_tracker_api.db.AbstractMyBatisTest;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeEntityMapperIntegrationTest extends AbstractMyBatisTest {

    @Autowired
    private EmployeeEntityMapper employeeEntityMapper;

    @Test
    void shouldInsertEmployeeAndFindById() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        EmployeeEntity employee = EmployeeEntity.builder()
                .login("test-user")
                .password("encoded")
                .age(25)
                .employeeRole(EmployeeRole.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        employeeEntityMapper.insert(employee);

        Optional<EmployeeEntity> foundOpt = employeeEntityMapper.findById(employee.getId());

        assertTrue(foundOpt.isPresent());

        EmployeeEntity found = foundOpt.get();

        assertEquals(employee.getLogin(), found.getLogin());
        assertEquals(employee.getPassword(), found.getPassword());
        assertEquals(employee.getAge(), found.getAge());
        assertEquals(employee.getEmployeeRole(), found.getEmployeeRole());

        assertEquals(
                now.toInstant(),
                found.getCreatedAt().toInstant().truncatedTo(ChronoUnit.MICROS)
        );

        assertEquals(
                now.toInstant(),
                found.getUpdatedAt().toInstant().truncatedTo(ChronoUnit.MICROS)
        );
    }

    @Test
    void shouldFindEmployeeByLogin() {
        OffsetDateTime now = OffsetDateTime.now();

        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .login("mapper-login")
                .password("encoded-password")
                .age(23)
                .employeeRole(EmployeeRole.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        employeeEntityMapper.insert(employeeEntity);

        Optional<EmployeeEntity> foundEmployeeOpt = employeeEntityMapper.findByLogin("mapper-login");

        assertTrue(foundEmployeeOpt.isPresent());

        EmployeeEntity foundEmployee = foundEmployeeOpt.get();

        assertEquals(employeeEntity.getId(), foundEmployee.getId());
        assertEquals("mapper-login", foundEmployee.getLogin());
    }

    @Test
    void shouldReturnTrueWhenEmployeeExistsByLogin() {
        OffsetDateTime now = OffsetDateTime.now();

        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .login("existing-login")
                .password("encoded-password")
                .age(24)
                .employeeRole(EmployeeRole.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        employeeEntityMapper.insert(employeeEntity);

        boolean exists = employeeEntityMapper.existsByLogin("existing-login");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenEmployeeDoesNotExistByLogin() {
        boolean exists = employeeEntityMapper.existsByLogin("missing-login");

        assertFalse(exists);
    }

    @Test
    void shouldReturnEmptyOptionalWhenEmployeeNotFoundById() {
        Optional<EmployeeEntity> foundEmployeeOpt = employeeEntityMapper.findById(Long.MAX_VALUE);

        assertTrue(foundEmployeeOpt.isEmpty());
    }

    @Test
    void shouldReturnEmptyOptionalWhenEmployeeNotFoundByLogin() {
        Optional<EmployeeEntity> foundEmployeeOpt = employeeEntityMapper.findByLogin("not-found-login");

        assertTrue(foundEmployeeOpt.isEmpty());
    }
}