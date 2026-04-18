package ru.haritonenko.task_time_tracker_api.employee.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.authorization.EmployeeCredentials;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.registration.EmployeeRegistration;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.response.EmployeeAuthDebugResponse;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.response.EmployeeDtoResponse;
import ru.haritonenko.task_time_tracker_api.employee.domain.mapper.EmployeeToDtoResponseMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.service.EmployeeService;
import ru.haritonenko.task_time_tracker_api.employee.security.jwt.response.JwtResponse;
import ru.haritonenko.task_time_tracker_api.employee.security.service.AuthenticationService;


import static java.util.Objects.isNull;

@Slf4j
@RestController
@RequestMapping("/api/employee")
@Validated
@RequiredArgsConstructor
@Tag(name = "Сотрудники", description = "Операции регистрации, авторизации и получения данных сотрудников")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeToDtoResponseMapper mapper;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Получить сотрудника по идентификатору",
            description = "Доступно только пользователю с ролью ADMIN",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сотрудник найден"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Сотрудник не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDtoResponse> getEmployeeById(
            @PathVariable("id") Long id
    ) {
        log.info("GET /api/employee/{}", id);
        var foundEmployee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(mapper.toDto(foundEmployee));
    }

    @Operation(
            summary = "Зарегистрировать сотрудника",
            description = "Создает нового сотрудника с ролью USER",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Сотрудник успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "409", description = "Сотрудник с таким логином уже существует")
    })
    @PostMapping
    public ResponseEntity<EmployeeDtoResponse> registerEmployee(
            @Valid @RequestBody EmployeeRegistration employeeFromSignUpRequest
    ) {
        log.info("POST /api/employee login={}", employeeFromSignUpRequest.login());
        var registeredEmployee = employeeService.register(employeeFromSignUpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toDto(registeredEmployee));
    }

    @Operation(
            summary = "Авторизовать сотрудника",
            description = "Проверяет логин и пароль и возвращает JWT токен",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT токен успешно выдан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    @PostMapping("/auth")
    public ResponseEntity<JwtResponse> authenticateEmployee(
            @Valid @RequestBody EmployeeCredentials employeeFromSignInRequest
    ) {
        log.info("POST /api/employee/auth login={}", employeeFromSignInRequest.login());
        var token = authenticationService.authenticate(employeeFromSignInRequest);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @Operation(
            summary = "Показать данные текущей аутентификации",
            description = "Диагностический эндпоинт для проверки имени пользователя и выданных прав",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о текущей аутентификации",
                    content = @Content(schema = @Schema(implementation = EmployeeAuthDebugResponse.class))
            )
    })
    @GetMapping("/debug/auth")
    public EmployeeAuthDebugResponse auth(@Parameter(hidden = true) Authentication authentication) {
        log.info("GET /api/employee/debug/auth");
        return new EmployeeAuthDebugResponse(
                isNull(authentication) ? null : authentication.getName(),
                isNull(authentication)
                        ? null
                        : authentication.getAuthorities().stream().map(Object::toString).toList()
        );
    }
}
