package ru.haritonenko.task_time_tracker_api.time_records.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import ru.haritonenko.task_time_tracker_api.config.PageConfig;
import ru.haritonenko.task_time_tracker_api.config.properties.CacheProperties;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification.AuthEmployee;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.entity.TaskEntity;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.mapper.TaskEntityMapper;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordCreateRequestDto;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.filter.TimeRecordPageFilter;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.filter.TimeRecordRequestFilterDto;
import ru.haritonenko.task_time_tracker_api.time_records.domain.TimeRecord;
import ru.haritonenko.task_time_tracker_api.time_records.domain.TimeRecordInfo;
import ru.haritonenko.task_time_tracker_api.time_records.domain.db.entity.TimeRecordEntity;
import ru.haritonenko.task_time_tracker_api.time_records.domain.db.mapper.TimeRecordEntityMapper;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.IllegalTimeRecordArgumentException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.IllegalTimeRecordStateException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.TaskOccupiedByAnotherEmployeeException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.TimeRecordNotFoundException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.mapper.TimeRecordToDomainMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceUnitTest {

    @Mock
    private TimeRecordEntityMapper timeRecordEntityMapper;
    @Mock
    private TimeRecordToDomainMapper timeRecordMapper;
    @Mock
    private EmployeeEntityMapper employeeEntityMapper;
    @Mock
    private TaskEntityMapper taskEntityMapper;
    @Mock
    private ObjectProvider<RedisTemplate<String, TimeRecord>> redisTimeRecordTemplateProvider;
    @Mock
    private CacheProperties cacheProperties;

    private TimeRecordService timeRecordService;
    private AuthEmployee userAuthEmployee;
    private AuthEmployee adminAuthEmployee;
    private EmployeeEntity employeeEntity;
    private TaskEntity doneTaskEntity;
    private TaskEntity newTaskEntity;
    private TimeRecordEntity timeRecordEntity;
    private TimeRecord timeRecordDomain;
    private TimeRecordCreateRequestDto createRequestDto;

    @BeforeEach
    void setUp() {
        when(redisTimeRecordTemplateProvider.getIfAvailable()).thenReturn(null);

        timeRecordService = new TimeRecordService(
                timeRecordEntityMapper,
                timeRecordMapper,
                employeeEntityMapper,
                taskEntityMapper,
                new PageConfig(0, 3),
                redisTimeRecordTemplateProvider,
                cacheProperties
        );

        userAuthEmployee = AuthEmployee.builder()
                .id(3L)
                .login("employee3")
                .role("USER")
                .build();

        adminAuthEmployee = AuthEmployee.builder()
                .id(1L)
                .login("admin")
                .role("ADMIN")
                .build();

        employeeEntity = EmployeeEntity.builder()
                .id(3L)
                .login("employee3")
                .password("encoded")
                .age(21)
                .employeeRole(EmployeeRole.USER)
                .build();

        doneTaskEntity = TaskEntity.builder()
                .id(100L)
                .title("done-task")
                .description("done-desc")
                .status(TaskStatus.DONE)
                .build();

        newTaskEntity = TaskEntity.builder()
                .id(101L)
                .title("new-task")
                .description("new-desc")
                .status(TaskStatus.NEW)
                .build();

        OffsetDateTime start = OffsetDateTime.parse("2026-04-10T09:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-04-10T11:00:00Z");
        OffsetDateTime created = OffsetDateTime.parse("2026-04-10T11:05:00Z");

        timeRecordEntity = TimeRecordEntity.builder()
                .id(1L)
                .employeeId(3L)
                .taskId(100L)
                .description("work")
                .startTime(start)
                .endTime(end)
                .createdAt(created)
                .updatedAt(created)
                .build();

        timeRecordDomain = new TimeRecord(
                1L,
                3L,
                100L,
                "work",
                start,
                end,
                created,
                created
        );

        createRequestDto = new TimeRecordCreateRequestDto(3L, 100L, "work", start, end);
    }

    @Test
    void shouldSuccessfullyCreateTimeRecord() {
        doAnswer(invocation -> {
            TimeRecordEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return null;
        }).when(timeRecordEntityMapper).insert(any(TimeRecordEntity.class));

        when(employeeEntityMapper.findById(3L)).thenReturn(Optional.of(employeeEntity));
        when(taskEntityMapper.findById(100L)).thenReturn(Optional.of(doneTaskEntity));
        when(timeRecordEntityMapper.existsRecordForTask(100L)).thenReturn(false);
        when(timeRecordMapper.toDomain(any(TimeRecordEntity.class))).thenReturn(timeRecordDomain);

        TimeRecord createdTimeRecord = timeRecordService.createTimeRecord(createRequestDto, userAuthEmployee);

        assertNotNull(createdTimeRecord.id());
        assertEquals(3L, createdTimeRecord.employeeId());
        assertEquals(100L, createdTimeRecord.taskId());
        assertEquals("work", createdTimeRecord.description());

        verify(timeRecordEntityMapper).insert(any(TimeRecordEntity.class));
        verify(timeRecordMapper).toDomain(any(TimeRecordEntity.class));
        verify(redisTimeRecordTemplateProvider).getIfAvailable();
    }

    @Test
    void shouldSuccessfullyGetTimeRecordById() {
        when(timeRecordEntityMapper.findById(1L)).thenReturn(Optional.of(timeRecordEntity));
        when(timeRecordMapper.toDomain(timeRecordEntity)).thenReturn(timeRecordDomain);

        TimeRecord foundTimeRecord = timeRecordService.getTimeRecordById(1L, userAuthEmployee);

        assertEquals(1L, foundTimeRecord.id());
        assertEquals(3L, foundTimeRecord.employeeId());
        assertEquals(100L, foundTimeRecord.taskId());
    }

    @Test
    void shouldSuccessfullySearchTimeRecordsWithoutPeriod() {
        TimeRecordRequestFilterDto requestFilter = new TimeRecordRequestFilterDto(3L, null, null);

        when(employeeEntityMapper.findById(3L)).thenReturn(Optional.of(employeeEntity));
        when(timeRecordEntityMapper.findByEmployeeId(3L, 3, 0)).thenReturn(List.of(timeRecordEntity));
        when(timeRecordMapper.toDomain(timeRecordEntity)).thenReturn(timeRecordDomain);

        List<TimeRecord> result = timeRecordService.getTimeRecordsByEmployeeIdWithFilters(
                requestFilter,
                new TimeRecordPageFilter(null, null),
                userAuthEmployee
        );

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void shouldSuccessfullySearchTimeRecordsWithPeriod() {
        TimeRecordRequestFilterDto requestFilter = new TimeRecordRequestFilterDto(
                3L,
                OffsetDateTime.parse("2026-04-01T00:00:00Z"),
                OffsetDateTime.parse("2026-04-30T23:59:59Z")
        );

        when(employeeEntityMapper.findById(3L)).thenReturn(Optional.of(employeeEntity));
        when(timeRecordEntityMapper.findByEmployeeIdAndPeriod(
                3L,
                requestFilter.startTime(),
                requestFilter.endTime(),
                5,
                0
        )).thenReturn(List.of(timeRecordEntity));
        when(timeRecordMapper.toDomain(timeRecordEntity)).thenReturn(timeRecordDomain);

        List<TimeRecord> result = timeRecordService.getTimeRecordsByEmployeeIdWithFilters(
                requestFilter,
                new TimeRecordPageFilter(0, 5),
                userAuthEmployee
        );

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void shouldSuccessfullyGetTimeRecordInfoWithoutPeriod() {
        TimeRecordRequestFilterDto requestFilter = new TimeRecordRequestFilterDto(3L, null, null);

        TimeRecordEntity secondRecord = TimeRecordEntity.builder()
                .id(2L)
                .employeeId(3L)
                .taskId(101L)
                .description("work-2")
                .startTime(OffsetDateTime.parse("2026-04-11T09:00:00Z"))
                .endTime(OffsetDateTime.parse("2026-04-11T10:00:00Z"))
                .createdAt(OffsetDateTime.parse("2026-04-11T10:05:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-04-11T10:05:00Z"))
                .build();

        when(employeeEntityMapper.findById(3L)).thenReturn(Optional.of(employeeEntity));
        when(timeRecordEntityMapper.findByEmployeeId(3L, Integer.MAX_VALUE, 0))
                .thenReturn(List.of(timeRecordEntity, secondRecord));
        when(taskEntityMapper.findById(100L)).thenReturn(Optional.of(doneTaskEntity));
        when(taskEntityMapper.findById(101L)).thenReturn(Optional.of(newTaskEntity));

        TimeRecordInfo info = timeRecordService.getTimeRecordInfoByEmployeeIdWithFilters(
                requestFilter,
                userAuthEmployee
        );

        assertEquals(3L, info.employeeId());
        assertEquals(2L, info.totalTasksCount());
        assertEquals(1L, info.totalDoneTasksCount());
        assertEquals(120L, info.totalSpentMinutes());
        assertNull(info.firstTimeBoundPeriod());
        assertNull(info.secondTimeBoundPeriod());
    }

    @Test
    void shouldSuccessfullyGetTimeRecordInfoWithPeriod() {
        TimeRecordRequestFilterDto requestFilter = new TimeRecordRequestFilterDto(
                3L,
                OffsetDateTime.parse("2026-04-01T00:00:00Z"),
                OffsetDateTime.parse("2026-04-30T23:59:59Z")
        );

        when(employeeEntityMapper.findById(3L)).thenReturn(Optional.of(employeeEntity));
        when(timeRecordEntityMapper.findByEmployeeIdAndPeriod(
                3L,
                requestFilter.startTime(),
                requestFilter.endTime(),
                Integer.MAX_VALUE,
                0
        )).thenReturn(List.of(timeRecordEntity));
        when(taskEntityMapper.findById(100L)).thenReturn(Optional.of(doneTaskEntity));

        TimeRecordInfo info = timeRecordService.getTimeRecordInfoByEmployeeIdWithFilters(
                requestFilter,
                userAuthEmployee
        );

        assertEquals(3L, info.employeeId());
        assertEquals(1L, info.totalTasksCount());
        assertEquals(1L, info.totalDoneTasksCount());
        assertEquals(120L, info.totalSpentMinutes());
        assertEquals(requestFilter.startTime(), info.firstTimeBoundPeriod());
        assertEquals(requestFilter.endTime(), info.secondTimeBoundPeriod());
    }

    @Test
    void shouldThrowIllegalTimeRecordArgumentExceptionWhenCreateRequestIsNull() {
        IllegalTimeRecordArgumentException exception = assertThrows(
                IllegalTimeRecordArgumentException.class,
                () -> timeRecordService.createTimeRecord(null, userAuthEmployee)
        );

        assertEquals("Time record create request is null", exception.getMessage());
    }

    @Test
    void shouldThrowTaskOccupiedByAnotherEmployeeExceptionWhenTaskAlreadyHasTimeRecord() {
        when(employeeEntityMapper.findById(3L)).thenReturn(Optional.of(employeeEntity));
        when(taskEntityMapper.findById(100L)).thenReturn(Optional.of(doneTaskEntity));
        when(timeRecordEntityMapper.existsRecordForTask(100L)).thenReturn(true);

        TaskOccupiedByAnotherEmployeeException exception = assertThrows(
                TaskOccupiedByAnotherEmployeeException.class,
                () -> timeRecordService.createTimeRecord(createRequestDto, adminAuthEmployee)
        );

        assertEquals("Task with id=100 already has a time record", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalTimeRecordStateExceptionWhenTimeRecordIdWasNotSaved() {
        when(employeeEntityMapper.findById(3L)).thenReturn(Optional.of(employeeEntity));
        when(taskEntityMapper.findById(100L)).thenReturn(Optional.of(doneTaskEntity));
        when(timeRecordEntityMapper.existsRecordForTask(100L)).thenReturn(false);

        IllegalTimeRecordStateException exception = assertThrows(
                IllegalTimeRecordStateException.class,
                () -> timeRecordService.createTimeRecord(createRequestDto, adminAuthEmployee)
        );

        assertEquals("Time record id was not saved", exception.getMessage());
    }

    @Test
    void shouldThrowTimeRecordNotFoundExceptionWhenTimeRecordNotFound() {
        when(timeRecordEntityMapper.findById(999L)).thenReturn(Optional.empty());

        TimeRecordNotFoundException exception = assertThrows(
                TimeRecordNotFoundException.class,
                () -> timeRecordService.getTimeRecordById(999L, userAuthEmployee)
        );

        assertEquals("Time record with id=999 not found", exception.getMessage());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUserRequestsAnotherEmployeesTimeRecord() {
        TimeRecordEntity anotherEmployeeRecord = timeRecordEntity.toBuilder()
                .employeeId(4L)
                .build();

        when(timeRecordEntityMapper.findById(1L)).thenReturn(Optional.of(anotherEmployeeRecord));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> timeRecordService.getTimeRecordById(1L, userAuthEmployee)
        );

        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalTimeRecordArgumentExceptionWhenOnlyOnePeriodBorderPassed() {
        TimeRecordRequestFilterDto requestFilter = new TimeRecordRequestFilterDto(
                3L,
                OffsetDateTime.parse("2026-04-01T00:00:00Z"),
                null
        );

        IllegalTimeRecordArgumentException exception = assertThrows(
                IllegalTimeRecordArgumentException.class,
                () -> timeRecordService.getTimeRecordInfoByEmployeeIdWithFilters(requestFilter, userAuthEmployee)
        );

        assertEquals("Start time and end time must be filled together", exception.getMessage());
    }
}