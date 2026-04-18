package ru.haritonenko.task_time_tracker_api.time_records.domain.db.mapper;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import ru.haritonenko.task_time_tracker_api.db.AbstractMyBatisTest;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.entity.TaskEntity;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.mapper.TaskEntityMapper;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;
import ru.haritonenko.task_time_tracker_api.time_records.domain.db.entity.TimeRecordEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TimeRecordEntityMapperIntegrationTest extends AbstractMyBatisTest {

    @Autowired
    private TimeRecordEntityMapper timeRecordEntityMapper;

    @Autowired
    private EmployeeEntityMapper employeeEntityMapper;

    @Autowired
    private TaskEntityMapper taskEntityMapper;

    @Test
    void shouldInsertTimeRecordAndFindById() {
        Long employeeId = createEmployee("mapper-employee-1");
        Long taskId = createTask("mapper-task-1", TaskStatus.NEW);

        OffsetDateTime startTime = OffsetDateTime.parse("2026-04-16T09:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2026-04-16T11:00:00Z");
        OffsetDateTime now = OffsetDateTime.parse("2026-04-16T11:05:00Z");

        TimeRecordEntity timeRecordEntity = TimeRecordEntity.builder()
                .employeeId(employeeId)
                .taskId(taskId)
                .description("mapper-time-record")
                .startTime(startTime)
                .endTime(endTime)
                .createdAt(now)
                .updatedAt(now)
                .build();

        timeRecordEntityMapper.insert(timeRecordEntity);

        assertNotNull(timeRecordEntity.getId());

        Optional<TimeRecordEntity> foundTimeRecordOpt = timeRecordEntityMapper.findById(timeRecordEntity.getId());

        assertTrue(foundTimeRecordOpt.isPresent());

        TimeRecordEntity foundTimeRecord = foundTimeRecordOpt.get();

        assertEquals(timeRecordEntity.getId(), foundTimeRecord.getId());
        assertEquals(employeeId, foundTimeRecord.getEmployeeId());
        assertEquals(taskId, foundTimeRecord.getTaskId());
        assertEquals("mapper-time-record", foundTimeRecord.getDescription());
        assertEquals(startTime.toInstant(), foundTimeRecord.getStartTime().toInstant());
        assertEquals(endTime.toInstant(), foundTimeRecord.getEndTime().toInstant());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTimeRecordNotFoundById() {
        Optional<TimeRecordEntity> foundTimeRecordOpt = timeRecordEntityMapper.findById(Long.MAX_VALUE);

        assertTrue(foundTimeRecordOpt.isEmpty());
    }

    @Test
    void shouldFindTimeRecordsByEmployeeId() {
        Long employeeId = createEmployee("mapper-employee-2");
        Long taskId1 = createTask("mapper-task-2", TaskStatus.NEW);
        Long taskId2 = createTask("mapper-task-3", TaskStatus.DONE);

        insertTimeRecord(
                employeeId,
                taskId1,
                "record-1",
                OffsetDateTime.parse("2026-04-10T09:00:00Z"),
                OffsetDateTime.parse("2026-04-10T10:00:00Z")
        );

        insertTimeRecord(
                employeeId,
                taskId2,
                "record-2",
                OffsetDateTime.parse("2026-04-12T09:00:00Z"),
                OffsetDateTime.parse("2026-04-12T11:00:00Z")
        );

        List<TimeRecordEntity> foundTimeRecords = timeRecordEntityMapper.findByEmployeeId(
                employeeId,
                10,
                0
        );

        assertEquals(2, foundTimeRecords.size());
    }

    @Test
    void shouldFindTimeRecordsByEmployeeIdAndPeriod() {
        Long employeeId = createEmployee("mapper-employee-3");
        Long taskId1 = createTask("mapper-task-4", TaskStatus.NEW);
        Long taskId2 = createTask("mapper-task-5", TaskStatus.DONE);

        insertTimeRecord(
                employeeId,
                taskId1,
                "record-1",
                OffsetDateTime.parse("2026-04-10T09:00:00Z"),
                OffsetDateTime.parse("2026-04-10T10:00:00Z")
        );

        insertTimeRecord(
                employeeId,
                taskId2,
                "record-2",
                OffsetDateTime.parse("2026-04-12T09:00:00Z"),
                OffsetDateTime.parse("2026-04-12T11:00:00Z")
        );

        List<TimeRecordEntity> foundTimeRecords = timeRecordEntityMapper.findByEmployeeIdAndPeriod(
                employeeId,
                OffsetDateTime.parse("2026-04-11T00:00:00Z"),
                OffsetDateTime.parse("2026-04-13T00:00:00Z"),
                10,
                0
        );

        assertEquals(1, foundTimeRecords.size());
        assertEquals("record-2", foundTimeRecords.get(0).getDescription());
    }

    @Test
    void shouldReturnTrueWhenTimeRecordExistsForTask() {
        Long employeeId = createEmployee("mapper-employee-4");
        Long taskId = createTask("task-with-record", TaskStatus.NEW);

        insertTimeRecord(
                employeeId,
                taskId,
                "existing-record",
                OffsetDateTime.parse("2026-04-15T09:00:00Z"),
                OffsetDateTime.parse("2026-04-15T10:00:00Z")
        );

        boolean exists = timeRecordEntityMapper.existsRecordForTask(taskId);

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenTimeRecordDoesNotExistForTask() {
        Long taskId = createTask("task-without-record", TaskStatus.NEW);

        boolean exists = timeRecordEntityMapper.existsRecordForTask(taskId);

        assertFalse(exists);
    }

    private Long createEmployee(String login) {
        OffsetDateTime now = OffsetDateTime.now();

        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .login(login)
                .password("encoded-password")
                .age(22)
                .employeeRole(EmployeeRole.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        employeeEntityMapper.insert(employeeEntity);
        return employeeEntity.getId();
    }

    private Long createTask(String title, TaskStatus status) {
        OffsetDateTime now = OffsetDateTime.now();

        TaskEntity taskEntity = TaskEntity.builder()
                .title(title)
                .description(title + "-description")
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();

        taskEntityMapper.insert(taskEntity);
        return taskEntity.getId();
    }

    private void insertTimeRecord(
            Long employeeId,
            Long taskId,
            String description,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        TimeRecordEntity timeRecordEntity = TimeRecordEntity.builder()
                .employeeId(employeeId)
                .taskId(taskId)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .createdAt(endTime.plusMinutes(5))
                .updatedAt(endTime.plusMinutes(5))
                .build();

        timeRecordEntityMapper.insert(timeRecordEntity);
    }
}