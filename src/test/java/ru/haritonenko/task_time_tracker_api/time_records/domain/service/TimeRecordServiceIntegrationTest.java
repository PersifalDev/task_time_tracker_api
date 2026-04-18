package ru.haritonenko.task_time_tracker_api.time_records.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.task_time_tracker_api.config.PageConfig;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification.AuthEmployee;
import ru.haritonenko.task_time_tracker_api.integration.AbstractIntegrationTest;
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
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.TaskOccupiedByAnotherEmployeeException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.TimeRecordNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeRecordServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TimeRecordService timeRecordService;

    @Autowired
    private TimeRecordEntityMapper timeRecordEntityMapper;

    @Autowired
    private EmployeeEntityMapper employeeEntityMapper;

    @Autowired
    private TaskEntityMapper taskEntityMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PageConfig pageConfig;

    @Transactional
    @Test
    void shouldSuccessfullyCreateTimeRecord() {
        EmployeeEntity employeeEntity = saveDummyEmployee("time_record_employee_create");
        TaskEntity taskEntity = saveDummyTask(TaskStatus.NEW);
        AuthEmployee authEmployee = buildUserAuthEmployee(employeeEntity.getId());

        TimeRecordCreateRequestDto createRequestDto = new TimeRecordCreateRequestDto(
                employeeEntity.getId(),
                taskEntity.getId(),
                "integration-time-record",
                OffsetDateTime.parse("2026-05-01T10:00:00Z"),
                OffsetDateTime.parse("2026-05-01T12:00:00Z")
        );

        TimeRecord createdTimeRecord = timeRecordService.createTimeRecord(createRequestDto, authEmployee);

        assertNotNull(createdTimeRecord.id());
        assertEquals(employeeEntity.getId(), createdTimeRecord.employeeId());
        assertEquals(taskEntity.getId(), createdTimeRecord.taskId());
        assertEquals("integration-time-record", createdTimeRecord.description());

        TimeRecordEntity foundTimeRecordEntity = timeRecordEntityMapper.findById(createdTimeRecord.id()).orElseThrow();
        assertEquals(employeeEntity.getId(), foundTimeRecordEntity.getEmployeeId());
        assertEquals(taskEntity.getId(), foundTimeRecordEntity.getTaskId());
    }

    @Transactional
    @Test
    void shouldSuccessfullyGetTimeRecordById() {
        EmployeeEntity employeeEntity = saveDummyEmployee("time_record_employee_get");
        TaskEntity taskEntity = saveDummyTask(TaskStatus.DONE);
        TimeRecordEntity savedTimeRecordEntity = saveDummyTimeRecord(employeeEntity.getId(), taskEntity.getId());

        TimeRecord foundTimeRecord = timeRecordService.getTimeRecordById(
                savedTimeRecordEntity.getId(),
                buildUserAuthEmployee(employeeEntity.getId())
        );

        assertEquals(savedTimeRecordEntity.getId(), foundTimeRecord.id());
        assertEquals(savedTimeRecordEntity.getEmployeeId(), foundTimeRecord.employeeId());
        assertEquals(savedTimeRecordEntity.getTaskId(), foundTimeRecord.taskId());
        assertEquals(savedTimeRecordEntity.getDescription(), foundTimeRecord.description());
    }

    @Transactional
    @Test
    void shouldSuccessfullyGetTimeRecordsByEmployeeIdWithFilters() {
        EmployeeEntity employeeEntity = saveDummyEmployee("time_record_employee_search");
        TaskEntity taskOne = saveDummyTask(TaskStatus.DONE);
        TaskEntity taskTwo = saveDummyTask(TaskStatus.NEW);
        saveDummyTimeRecord(employeeEntity.getId(), taskOne.getId());
        saveDummyTimeRecord(employeeEntity.getId(), taskTwo.getId(), OffsetDateTime.parse("2026-05-02T10:00:00Z"), OffsetDateTime.parse("2026-05-02T11:00:00Z"));

        TimeRecordRequestFilterDto requestFilterDto = new TimeRecordRequestFilterDto(
                employeeEntity.getId(),
                OffsetDateTime.parse("2026-05-01T00:00:00Z"),
                OffsetDateTime.parse("2026-05-03T00:00:00Z")
        );

        List<TimeRecord> timeRecords = timeRecordService.getTimeRecordsByEmployeeIdWithFilters(
                requestFilterDto,
                new TimeRecordPageFilter(0, 10),
                buildUserAuthEmployee(employeeEntity.getId())
        );

        assertEquals(2, timeRecords.size());
    }

    @Transactional
    @Test
    void shouldSuccessfullyGetTimeRecordInfoByEmployeeIdWithFilters() {
        EmployeeEntity employeeEntity = saveDummyEmployee("time_record_employee_info");
        TaskEntity doneTask = saveDummyTask(TaskStatus.DONE);
        TaskEntity newTask = saveDummyTask(TaskStatus.NEW);

        saveDummyTimeRecord(
                employeeEntity.getId(),
                doneTask.getId(),
                OffsetDateTime.parse("2026-05-10T10:00:00Z"),
                OffsetDateTime.parse("2026-05-10T12:00:00Z")
        );
        saveDummyTimeRecord(
                employeeEntity.getId(),
                newTask.getId(),
                OffsetDateTime.parse("2026-05-11T10:00:00Z"),
                OffsetDateTime.parse("2026-05-11T11:30:00Z")
        );

        TimeRecordInfo timeRecordInfo = timeRecordService.getTimeRecordInfoByEmployeeIdWithFilters(
                new TimeRecordRequestFilterDto(
                        employeeEntity.getId(),
                        OffsetDateTime.parse("2026-05-10T00:00:00Z"),
                        OffsetDateTime.parse("2026-05-11T23:59:59Z")
                ),
                buildUserAuthEmployee(employeeEntity.getId())
        );

        assertEquals(employeeEntity.getId(), timeRecordInfo.employeeId());
        assertEquals(2L, timeRecordInfo.totalTasksCount());
        assertEquals(1L, timeRecordInfo.totalDoneTasksCount());
        assertEquals(120L, timeRecordInfo.totalSpentMinutes());
    }

    @Transactional
    @Test
    void shouldUseDefaultPagingWhenPageFilterIsNull() {
        EmployeeEntity employeeEntity = saveDummyEmployee("time_record_employee_default_page");
        TaskEntity taskOne = saveDummyTask(TaskStatus.DONE);
        TaskEntity taskTwo = saveDummyTask(TaskStatus.NEW);
        TaskEntity taskThree = saveDummyTask(TaskStatus.DONE);
        TaskEntity taskFour = saveDummyTask(TaskStatus.NEW);
        saveDummyTimeRecord(employeeEntity.getId(), taskOne.getId());
        saveDummyTimeRecord(employeeEntity.getId(), taskTwo.getId(), OffsetDateTime.parse("2026-05-02T10:00:00Z"), OffsetDateTime.parse("2026-05-02T11:00:00Z"));
        saveDummyTimeRecord(employeeEntity.getId(), taskThree.getId(), OffsetDateTime.parse("2026-05-03T10:00:00Z"), OffsetDateTime.parse("2026-05-03T11:00:00Z"));
        saveDummyTimeRecord(employeeEntity.getId(), taskFour.getId(), OffsetDateTime.parse("2026-05-04T10:00:00Z"), OffsetDateTime.parse("2026-05-04T11:00:00Z"));

        List<TimeRecord> timeRecords = timeRecordService.getTimeRecordsByEmployeeIdWithFilters(
                new TimeRecordRequestFilterDto(employeeEntity.getId(), null, null),
                null,
                buildUserAuthEmployee(employeeEntity.getId())
        );

        assertEquals(pageConfig.defaultPageSize(), timeRecords.size());
    }

    @Transactional
    @Test
    void shouldThrowIllegalTimeRecordArgumentExceptionWhenCreateRequestIsNull() {
        IllegalTimeRecordArgumentException exception = assertThrows(
                IllegalTimeRecordArgumentException.class,
                () -> timeRecordService.createTimeRecord(null, buildAdminAuthEmployee())
        );

        assertEquals("Time record create request is null", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowTaskOccupiedByAnotherEmployeeExceptionWhenTaskAlreadyHasTimeRecord() {
        EmployeeEntity employeeOne = saveDummyEmployee("time_record_employee_busy_1");
        EmployeeEntity employeeTwo = saveDummyEmployee("time_record_employee_busy_2");
        TaskEntity taskEntity = saveDummyTask(TaskStatus.NEW);

        saveDummyTimeRecord(
                employeeOne.getId(),
                taskEntity.getId(),
                OffsetDateTime.parse("2026-05-01T10:00:00Z"),
                OffsetDateTime.parse("2026-05-01T12:00:00Z")
        );

        TimeRecordCreateRequestDto createRequestDto = new TimeRecordCreateRequestDto(
                employeeTwo.getId(),
                taskEntity.getId(),
                "busy-task",
                OffsetDateTime.parse("2026-05-01T10:30:00Z"),
                OffsetDateTime.parse("2026-05-01T11:30:00Z")
        );

        TaskOccupiedByAnotherEmployeeException exception = assertThrows(
                TaskOccupiedByAnotherEmployeeException.class,
                () -> timeRecordService.createTimeRecord(createRequestDto, buildAdminAuthEmployee())
        );

        assertEquals("Task with id=%d already has a time record".formatted(taskEntity.getId()), exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowTimeRecordNotFoundExceptionWhenTimeRecordMissingById() {
        TimeRecordNotFoundException exception = assertThrows(
                TimeRecordNotFoundException.class,
                () -> timeRecordService.getTimeRecordById(Long.MAX_VALUE, buildAdminAuthEmployee())
        );

        assertEquals("Time record with id=%d not found".formatted(Long.MAX_VALUE), exception.getMessage());
    }

    private EmployeeEntity saveDummyEmployee(String login) {
        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .login(login)
                .password(passwordEncoder.encode("password123"))
                .age(25)
                .employeeRole(EmployeeRole.USER)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        employeeEntityMapper.insert(employeeEntity);
        return employeeEntity;
    }

    private TaskEntity saveDummyTask(TaskStatus status) {
        TaskEntity taskEntity = TaskEntity.builder()
                .title("task-" + System.nanoTime())
                .description("task-description")
                .status(status)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        taskEntityMapper.insert(taskEntity);
        return taskEntity;
    }

    private TimeRecordEntity saveDummyTimeRecord(Long employeeId, Long taskId) {
        return saveDummyTimeRecord(
                employeeId,
                taskId,
                OffsetDateTime.parse("2026-05-01T10:00:00Z"),
                OffsetDateTime.parse("2026-05-01T11:00:00Z")
        );
    }

    private TimeRecordEntity saveDummyTimeRecord(Long employeeId, Long taskId, OffsetDateTime startTime, OffsetDateTime endTime) {
        TimeRecordEntity timeRecordEntity = TimeRecordEntity.builder()
                .employeeId(employeeId)
                .taskId(taskId)
                .description("time-record-description")
                .startTime(startTime)
                .endTime(endTime)
                .createdAt(endTime)
                .updatedAt(endTime)
                .build();
        timeRecordEntityMapper.insert(timeRecordEntity);
        return timeRecordEntity;
    }

    private AuthEmployee buildUserAuthEmployee(Long employeeId) {
        return AuthEmployee.builder()
                .id(employeeId)
                .login("employee")
                .role("USER")
                .build();
    }

    private AuthEmployee buildAdminAuthEmployee() {
        return AuthEmployee.builder()
                .id(1L)
                .login("admin")
                .role("ADMIN")
                .build();
    }
}
