package ru.haritonenko.task_time_tracker_api.tasks.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.task_time_tracker_api.integration.AbstractIntegrationTest;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.TaskCreateRequestDto;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.TaskUpdateRequestDto;
import ru.haritonenko.task_time_tracker_api.tasks.domain.Task;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.entity.TaskEntity;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.mapper.TaskEntityMapper;
import ru.haritonenko.task_time_tracker_api.tasks.domain.exception.IllegalTaskArgumentException;
import ru.haritonenko.task_time_tracker_api.tasks.domain.exception.TaskNotFoundException;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskEntityMapper taskEntityMapper;

    @Transactional
    @Test
    void shouldSuccessfullyCreateTask() {
        TaskCreateRequestDto requestDto = new TaskCreateRequestDto(
                "integration-task",
                "integration-description"
        );

        Task createdTask = taskService.createTask(requestDto);

        assertNotNull(createdTask.id());
        assertEquals(requestDto.title(), createdTask.title());
        assertEquals(requestDto.description(), createdTask.description());
        assertEquals(TaskStatus.NEW, createdTask.status());

        TaskEntity foundTaskEntity = taskEntityMapper.findById(createdTask.id()).orElseThrow();
        assertEquals(requestDto.title(), foundTaskEntity.getTitle());
        assertEquals(requestDto.description(), foundTaskEntity.getDescription());
        assertEquals(TaskStatus.NEW, foundTaskEntity.getStatus());
    }

    @Transactional
    @Test
    void shouldSuccessfullyGetTaskById() {
        TaskEntity savedTaskEntity = saveDummyTask();

        Task foundTask = taskService.getTaskById(savedTaskEntity.getId());

        assertEquals(savedTaskEntity.getId(), foundTask.id());
        assertEquals(savedTaskEntity.getTitle(), foundTask.title());
        assertEquals(savedTaskEntity.getDescription(), foundTask.description());
        assertEquals(savedTaskEntity.getStatus(), foundTask.status());
    }

    @Transactional
    @Test
    void shouldSuccessfullyChangeTaskStatusById() {
        TaskEntity savedTaskEntity = saveDummyTask();

        Task updatedTask = taskService.changeTaskStatusById(
                savedTaskEntity.getId(),
                new TaskUpdateRequestDto(TaskStatus.DONE)
        );

        assertEquals(savedTaskEntity.getId(), updatedTask.id());
        assertEquals(TaskStatus.DONE, updatedTask.status());

        TaskEntity updatedTaskEntity = taskEntityMapper.findById(savedTaskEntity.getId()).orElseThrow();
        assertEquals(TaskStatus.DONE, updatedTaskEntity.getStatus());
    }

    @Transactional
    @Test
    void shouldThrowIllegalTaskArgumentExceptionWhenCreateRequestIsNull() {
        IllegalTaskArgumentException exception = assertThrows(
                IllegalTaskArgumentException.class,
                () -> taskService.createTask(null)
        );

        assertEquals("Task create request is null", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalTaskArgumentExceptionWhenTaskIdIsNull() {
        IllegalTaskArgumentException exception = assertThrows(
                IllegalTaskArgumentException.class,
                () -> taskService.getTaskById(null)
        );

        assertEquals("Task id is null", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowTaskNotFoundExceptionWhenTaskNotFoundById() {
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(Long.MAX_VALUE)
        );

        assertEquals("Task with id=%d not found".formatted(Long.MAX_VALUE), exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalTaskArgumentExceptionWhenTaskIdOrRequestIsNull() {
        IllegalTaskArgumentException exceptionForNullId = assertThrows(
                IllegalTaskArgumentException.class,
                () -> taskService.changeTaskStatusById(null, new TaskUpdateRequestDto(TaskStatus.DONE))
        );

        assertEquals("Task id or request is null", exceptionForNullId.getMessage());

        IllegalTaskArgumentException exceptionForNullRequest = assertThrows(
                IllegalTaskArgumentException.class,
                () -> taskService.changeTaskStatusById(1L, null)
        );

        assertEquals("Task id or request is null", exceptionForNullRequest.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowTaskNotFoundExceptionWhenChangingStatusOfMissingTask() {
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.changeTaskStatusById(Long.MAX_VALUE, new TaskUpdateRequestDto(TaskStatus.DONE))
        );

        assertEquals("Task with id=%d not found".formatted(Long.MAX_VALUE), exception.getMessage());
    }

    private TaskEntity saveDummyTask() {
        TaskEntity taskEntity = TaskEntity.builder()
                .title("dummy-task")
                .description("dummy-description")
                .status(TaskStatus.NEW)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        taskEntityMapper.insert(taskEntity);
        return taskEntity;
    }
}
