package ru.haritonenko.task_time_tracker_api.time_records.api.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification.AuthEmployee;
import ru.haritonenko.task_time_tracker_api.employee.security.service.AuthenticationService;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordCreateRequestDto;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordResponseDto;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordTimePeriodResponseDto;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.filter.TimeRecordPageFilter;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.filter.TimeRecordRequestFilterDto;
import ru.haritonenko.task_time_tracker_api.time_records.domain.mapper.TimeRecordToResponseDtoMapper;
import ru.haritonenko.task_time_tracker_api.time_records.domain.mapper.TimeRecordToTimePeriodResponseDtoMapper;
import ru.haritonenko.task_time_tracker_api.time_records.domain.service.TimeRecordService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/time-records")
@Tag(name = "Учет времени", description = "Операции создания записей времени, поиска и получения сводной информации")
public class TimeRecordController {

    private final TimeRecordService timeRecordService;
    private final TimeRecordToResponseDtoMapper mapper;
    private final TimeRecordToTimePeriodResponseDtoMapper periodResponseMapper;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Создать запись о затраченном времени",
            description = "Создает новую запись о времени сотрудника на задачу",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Запись времени успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Сущность не найдена"),
            @ApiResponse(responseCode = "409", description = "Конфликт бизнес-правил")
    })
    @PostMapping
    public ResponseEntity<TimeRecordResponseDto> createTimeRecord(
            @Valid @RequestBody TimeRecordCreateRequestDto createRequestDto
    ) {
        AuthEmployee authenticatedEmployee = getAuthenticatedUser();
        log.info(
                "POST /api/time-records employeeId={}, taskId={} by authenticatedEmployeeId={}, role={}",
                createRequestDto.employeeId(),
                createRequestDto.taskId(),
                authenticatedEmployee.id(),
                authenticatedEmployee.role()
        );

        var timeRecord = timeRecordService.createTimeRecord(createRequestDto, authenticatedEmployee);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponseDto(timeRecord));
    }

    @Operation(
            summary = "Получить сводную информацию по времени сотрудника",
            description = "Возвращает агрегированную информацию по количеству задач и потраченному времени",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сводная информация успешно получена"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Сотрудник не найден")
    })
    @PostMapping("/info")
    public ResponseEntity<TimeRecordTimePeriodResponseDto> getTimeRecordInfoByEmployeeIdWithFilters(
            @Valid @RequestBody TimeRecordRequestFilterDto requestFilter
    ) {
        AuthEmployee authenticatedEmployee = getAuthenticatedUser();
        log.info(
                "POST /api/time-records/info employeeId={}, startTime={}, endTime={} by authenticatedEmployeeId={}, role={}",
                requestFilter.employeeId(),
                requestFilter.startTime(),
                requestFilter.endTime(),
                authenticatedEmployee.id(),
                authenticatedEmployee.role()
        );

        var timeRecordInfo = timeRecordService.getTimeRecordInfoByEmployeeIdWithFilters(
                requestFilter,
                authenticatedEmployee
        );

        return ResponseEntity.ok(periodResponseMapper.toResponseDto(timeRecordInfo));
    }

    @Operation(
            summary = "Найти записи времени сотрудника",
            description = "Возвращает список записей времени по фильтрам и параметрам пагинации",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список записей успешно получен",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TimeRecordResponseDto.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Сотрудник не найден")
    })
    @PostMapping("/search")
    public ResponseEntity<List<TimeRecordResponseDto>> getTimeRecordsByEmployeeIdWithFilters(
            @Valid @RequestBody TimeRecordRequestFilterDto requestFilter,
            @Valid @ModelAttribute TimeRecordPageFilter pageFilter
    ) {
        AuthEmployee authenticatedEmployee = getAuthenticatedUser();
        log.info(
                "POST /api/time-records/search employeeId={}, startTime={}, endTime={}, pageNumber={}, pageSize={} by authenticatedEmployeeId={}, role={}",
                requestFilter.employeeId(),
                requestFilter.startTime(),
                requestFilter.endTime(),
                pageFilter.pageNumber(),
                pageFilter.pageSize(),
                authenticatedEmployee.id(),
                authenticatedEmployee.role()
        );

        var timeRecords = timeRecordService.getTimeRecordsByEmployeeIdWithFilters(
                requestFilter,
                pageFilter,
                authenticatedEmployee
        );

        return ResponseEntity.ok(
                timeRecords.stream()
                        .map(mapper::toResponseDto)
                        .toList()
        );
    }

    @Operation(
            summary = "Получить запись времени по идентификатору",
            description = "Возвращает одну запись времени по ее идентификатору",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Запись времени найдена"),
            @ApiResponse(responseCode = "401", description = "Не выполнена аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Запись времени не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TimeRecordResponseDto> getTimeRecordById(
            @PathVariable("id") Long id
    ) {
        AuthEmployee authenticatedEmployee = getAuthenticatedUser();
        log.info(
                "GET /api/time-records/{} by authenticatedEmployeeId={}, role={}",
                id,
                authenticatedEmployee.id(),
                authenticatedEmployee.role()
        );

        var timeRecord = timeRecordService.getTimeRecordById(id, authenticatedEmployee);
        return ResponseEntity.ok(mapper.toResponseDto(timeRecord));
    }

    private AuthEmployee getAuthenticatedUser() {
        return authenticationService.getCurrentAuthenticatedUser();
    }
}
