package ru.haritonenko.task_time_tracker_api.employee.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.authorization.EmployeeCredentials;
import ru.haritonenko.task_time_tracker_api.employee.domain.Employee;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeArgumentException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeStateException;
import ru.haritonenko.task_time_tracker_api.employee.domain.service.EmployeeService;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification.AuthEmployee;
import ru.haritonenko.task_time_tracker_api.employee.security.jwt.manager.JwtTokenManager;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;
    private final EmployeeService employeeService;

    public String authenticate(EmployeeCredentials employeeFromSignInRequest) {
        if (isNull(employeeFromSignInRequest)) {
            log.warn("Employee authentication request is null");
            throw new IllegalEmployeeArgumentException("Employee authentication request is null");
        }

        log.info("Authenticating employee with login={}", employeeFromSignInRequest.login());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        employeeFromSignInRequest.login(),
                        employeeFromSignInRequest.password()
                )
        );

        Employee employee = employeeService.findByLogin(employeeFromSignInRequest.login());

        log.info("Generating jwt token for employee id={}", employee.id());
        return jwtTokenManager.generateToken(
                employee.id(),
                employee.login(),
                employee.role().toString()
        );
    }

    public AuthEmployee getCurrentAuthenticatedUser() {
        log.info("Getting authenticated employee");
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isNull(authentication)) {
            log.warn("Authenticated employee is not present");
            throw new IllegalEmployeeStateException("Authentication not present");
        }

        return (AuthEmployee) authentication.getPrincipal();
    }
}