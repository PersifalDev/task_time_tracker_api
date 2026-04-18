package ru.haritonenko.task_time_tracker_api.employee.domain.db.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;

import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultEmployeesInitializer {

    private final EmployeeEntityMapper employeeEntityMapper;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        createIfNotExists("admin", "admin_password", EmployeeRole.ADMIN);
        createIfNotExists("pavlov", "pavlov_password", EmployeeRole.USER);
    }

    private void createIfNotExists(String login, String password, EmployeeRole role) {
        if (employeeEntityMapper.existsByLogin(login)) {
            log.warn("Default employee '{}' already exists, skipping", login);
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        EmployeeEntity entity = EmployeeEntity.builder()
                .login(login)
                .password(passwordEncoder.encode(password))
                .age(ThreadLocalRandom.current().nextInt(18,101))
                .employeeRole(role)
                .createdAt(now)
                .updatedAt(now)
                .build();

        employeeEntityMapper.insert(entity);
        log.info("Default employee '{}' created with role {}", login, role);
    }
}