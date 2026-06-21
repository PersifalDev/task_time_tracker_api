package ru.haritonenko.task_time_tracker_api.tasks.domain.db.mapper;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.haritonenko.task_time_tracker_api.db.AbstractMyBatisTest;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.entity.TaskEntity;
import ru.haritonenko.task_time_tracker_api.tasks.domain.priority.TaskPriority;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskEntityMapperIntegrationTest extends AbstractMyBatisTest {

    @Autowired
    private TaskEntityMapper taskEntityMapper;

    @Test
    void shouldInsertTaskAndFindById() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        TaskEntity taskEntity = TaskEntity.builder()
                .title("mapper-task")
                .description("mapper-description")
                .status(TaskStatus.NEW)
                .priority(TaskPriority.MEDIUM)
                .createdAt(now)
                .updatedAt(now)
                .build();

        taskEntityMapper.insert(taskEntity);

        assertNotNull(taskEntity.getId());

        Optional<TaskEntity> foundTaskOpt = taskEntityMapper.findById(taskEntity.getId());

        assertTrue(foundTaskOpt.isPresent());

        TaskEntity foundTask = foundTaskOpt.get();

        assertEquals(taskEntity.getId(), foundTask.getId());
        assertEquals("mapper-task", foundTask.getTitle());
        assertEquals("mapper-description", foundTask.getDescription());
        assertEquals(TaskStatus.NEW, foundTask.getStatus());
        assertEquals(TaskPriority.MEDIUM, foundTask.getPriority());
        assertEquals(
                now.toInstant(),
                foundTask.getCreatedAt().toInstant().truncatedTo(ChronoUnit.MICROS)
        );
        assertEquals(
                now.toInstant(),
                foundTask.getUpdatedAt().toInstant().truncatedTo(ChronoUnit.MICROS)
        );
    }

    @Test
    void shouldReturnEmptyOptionalWhenTaskNotFoundById() {
        Optional<TaskEntity> foundTaskOpt = taskEntityMapper.findById(Long.MAX_VALUE);

        assertTrue(foundTaskOpt.isEmpty());
    }

    @Test
    void shouldUpdateTaskStatus() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        TaskEntity taskEntity = TaskEntity.builder()
                .title("mapper-task-to-update")
                .description("mapper-description-to-update")
                .status(TaskStatus.NEW)
                .priority(TaskPriority.MEDIUM)
                .createdAt(now)
                .updatedAt(now)
                .build();

        taskEntityMapper.insert(taskEntity);

        OffsetDateTime updatedAt = now.plusHours(2).truncatedTo(ChronoUnit.MICROS);

        int updatedRows = taskEntityMapper.updateStatus(
                taskEntity.getId(),
                TaskStatus.DONE,
                updatedAt
        );

        assertEquals(1, updatedRows);

        TaskEntity updatedTask = taskEntityMapper.findById(taskEntity.getId()).orElseThrow();

        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
        assertEquals(
                updatedAt.toInstant(),
                updatedTask.getUpdatedAt().toInstant().truncatedTo(ChronoUnit.MICROS)
        );
    }

    @Test
    void shouldReturnZeroUpdatedRowsWhenTaskNotFoundDuringStatusUpdate() {
        int updatedRows = taskEntityMapper.updateStatus(
                Long.MAX_VALUE,
                TaskStatus.DONE,
                OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS)
        );

        assertEquals(0, updatedRows);
    }

    @Test
    void shouldFindTasksWithNullablePriorityAndStatusFilter() {
        List<TaskEntity> filteredByPriorityAndStatus = taskEntityMapper.findTasksWithPriorityAndStatusFilter(
                TaskPriority.MEDIUM.name(),
                TaskStatus.DONE.name(),
                10,
                0
        );
        List<TaskEntity> filteredByPriority = taskEntityMapper.findTasksWithPriorityAndStatusFilter(
                TaskPriority.MEDIUM.name(),
                null,
                10,
                0
        );
        List<TaskEntity> filteredByStatus = taskEntityMapper.findTasksWithPriorityAndStatusFilter(
                null,
                TaskStatus.DONE.name(),
                10,
                0
        );
        List<TaskEntity> withoutFilters = taskEntityMapper.findTasksWithPriorityAndStatusFilter(
                null,
                null,
                10,
                0
        );

        assertFalse(filteredByPriorityAndStatus.isEmpty());
        assertFalse(filteredByPriority.isEmpty());
        assertFalse(filteredByStatus.isEmpty());
        assertFalse(withoutFilters.isEmpty());
    }
}
