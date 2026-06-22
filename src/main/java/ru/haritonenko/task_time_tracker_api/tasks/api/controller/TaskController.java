package ru.haritonenko.task_time_tracker_api.tasks.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification.AuthEmployee;
import ru.haritonenko.task_time_tracker_api.employee.security.service.AuthenticationService;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.TaskCreateRequestDto;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.TaskResponseDto;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.TaskUpdateRequestDto;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.filter.TaskPageFilter;
import ru.haritonenko.task_time_tracker_api.tasks.domain.mapper.TaskToResponseDtoMapper;
import ru.haritonenko.task_time_tracker_api.tasks.domain.priority.TaskPriority;
import ru.haritonenko.task_time_tracker_api.tasks.domain.service.TaskService;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Задачи", description = "Операции создания задач, получения данных и изменения статуса")
public class TaskController {

    private final TaskService taskService;
    private final TaskToResponseDtoMapper mapper;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Создать задачу",
            description = "Создает новую задачу со статусом NEW",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskCreateRequestDto createRequestDto
    ) {
        AuthEmployee authenticatedEmployee = getAuthenticatedUser();
        log.info(
                "POST /api/tasks by employeeId={}, role={}",
                authenticatedEmployee.id(),
                authenticatedEmployee.role()
        );

        var task = taskService.createTask(createRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponseDto(task));
    }

    @Operation(
            summary = "Получить задачу по идентификатору",
            description = "Возвращает подробную информацию о задаче",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача найдена"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable("id") Long id
    ) {
        AuthEmployee authenticatedEmployee = getAuthenticatedUser();
        log.info(
                "GET /api/tasks/{} by employeeId={}, role={}",
                id,
                authenticatedEmployee.id(),
                authenticatedEmployee.role()
        );

        var task = taskService.getTaskById(id);
        return ResponseEntity.ok(mapper.toResponseDto(task));
    }

    @Operation(
            summary = "Изменить статус задачи",
            description = "Обновляет статус задачи по идентификатору",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус задачи успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "409", description = "Не удалось изменить статус задачи")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> changeTaskStatusById(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskUpdateRequestDto updateRequestDto
    ) {
        AuthEmployee authenticatedEmployee = getAuthenticatedUser();
        log.info(
                "PATCH /api/tasks/{}/status by employeeId={}, role={}",
                id,
                authenticatedEmployee.id(),
                authenticatedEmployee.role()
        );

        var task = taskService.changeTaskStatusById(id, updateRequestDto);
        return ResponseEntity.ok(mapper.toResponseDto(task));
    }

    @Operation(
            summary = "Найти задачи",
            description = "Возвращает список задач по фильтрам приоритета, статуса и параметрам пагинации",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список задач успешно получен",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/search")
    public ResponseEntity<List<TaskResponseDto>> getTasksWithPriorityAndStatusFilter(
            @Parameter(
                    description = "Приоритет задачи",
                    example = "HIGH",
                    schema = @Schema(implementation = TaskPriority.class)
            )
            @RequestParam(required = false) TaskPriority priority,
            @Parameter(
                    description = "Статус задачи",
                    example = "NEW",
                    schema = @Schema(implementation = TaskStatus.class)
            )
            @RequestParam(required = false) TaskStatus status,
            @ParameterObject @Valid @ModelAttribute TaskPageFilter taskPageFilter
    ) {
        AuthEmployee authenticatedEmployee = getAuthenticatedUser();
        log.info(
                "GET /api/tasks/search by employeeId={}, role={}",
                authenticatedEmployee.id(),
                authenticatedEmployee.role()
        );
        return ResponseEntity.ok(
                taskService.getTasksWithPriorityAndStatusFilter(
                priority,status,taskPageFilter.pageNumber(), taskPageFilter.pageSize())
                        .stream().map(mapper::toResponseDto).toList()
        );

    }

    private AuthEmployee getAuthenticatedUser() {
        return authenticationService.getCurrentAuthenticatedUser();
    }
}
