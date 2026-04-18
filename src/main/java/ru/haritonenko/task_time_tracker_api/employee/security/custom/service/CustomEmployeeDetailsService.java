package ru.haritonenko.task_time_tracker_api.employee.security.custom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomEmployeeDetailsService implements UserDetailsService {

    private final EmployeeEntityMapper employeeEntityMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading employee by login={}", username);

        var employee = employeeEntityMapper.findByLogin(username)
                .orElseThrow(() -> {
                    log.warn("Employee with login={} not found", username);
                    return new UsernameNotFoundException("Employee not found by login: %s".formatted(username));
                });

        log.info("Employee with login={} was successfully loaded", username);

        return User.withUsername(username)
                .password(employee.getPassword())
                .authorities(String.valueOf(employee.getEmployeeRole()))
                .build();
    }
}