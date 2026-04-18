package ru.haritonenko.task_time_tracker_api.tasks.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import ru.haritonenko.task_time_tracker_api.config.properties.CacheProperties;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.TaskCreateRequestDto;
import ru.haritonenko.task_time_tracker_api.tasks.api.dto.TaskUpdateRequestDto;
import ru.haritonenko.task_time_tracker_api.tasks.domain.Task;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.entity.TaskEntity;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.mapper.TaskEntityMapper;
import ru.haritonenko.task_time_tracker_api.tasks.domain.exception.IllegalTaskArgumentException;
import ru.haritonenko.task_time_tracker_api.tasks.domain.exception.IllegalTaskStateException;
import ru.haritonenko.task_time_tracker_api.tasks.domain.exception.TaskNotFoundException;
import ru.haritonenko.task_time_tracker_api.tasks.domain.mapper.TaskToDomainMapper;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceUnitTest {

    @Mock
    private TaskEntityMapper taskEntityMapper;
    @Mock
    private TaskToDomainMapper taskMapper;
    @Mock
    private ObjectProvider<RedisTemplate<String, Task>> redisTaskTemplateProvider;
    @Mock
    private CacheProperties cacheProperties;

    private TaskService taskService;

    private TaskEntity taskEntity;
    private Task taskDomain;
    private TaskCreateRequestDto createRequestDto;
    private TaskUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        when(redisTaskTemplateProvider.getIfAvailable()).thenReturn(null);

        taskService = new TaskService(
                taskEntityMapper,
                taskMapper,
                redisTaskTemplateProvider,
                cacheProperties
        );

        OffsetDateTime now = OffsetDateTime.parse("2026-04-16T10:00:00Z");

        taskEntity = TaskEntity.builder()
                .id(1L)
                .title("test-task")
                .description("test-description")
                .status(TaskStatus.NEW)
                .createdAt(now)
                .updatedAt(now)
                .build();

        taskDomain = new Task(1L, "test-task", "test-description", TaskStatus.NEW, now, now);
        createRequestDto = new TaskCreateRequestDto("test-task", "test-description");
        updateRequestDto = new TaskUpdateRequestDto(TaskStatus.DONE);
    }

    @Test
    void shouldSuccessfullyCreateTask() {
        doAnswer(invocation -> {
            TaskEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return null;
        }).when(taskEntityMapper).insert(any(TaskEntity.class));

        when(taskMapper.toDomain(any(TaskEntity.class))).thenReturn(taskDomain);

        Task createdTask = taskService.createTask(createRequestDto);

        assertNotNull(createdTask.id());
        assertEquals("test-task", createdTask.title());
        assertEquals("test-description", createdTask.description());
        assertEquals(TaskStatus.NEW, createdTask.status());

        verify(taskEntityMapper).insert(any(TaskEntity.class));
        verify(taskMapper).toDomain(any(TaskEntity.class));
        verify(redisTaskTemplateProvider).getIfAvailable();
    }

    @Test
    void shouldSuccessfullyGetTaskById() {
        when(taskEntityMapper.findById(1L)).thenReturn(Optional.of(taskEntity));
        when(taskMapper.toDomain(taskEntity)).thenReturn(taskDomain);

        Task foundTask = taskService.getTaskById(1L);

        assertEquals(taskDomain.id(), foundTask.id());
        assertEquals(taskDomain.title(), foundTask.title());
        assertEquals(taskDomain.description(), foundTask.description());
        assertEquals(taskDomain.status(), foundTask.status());

        verify(taskEntityMapper).findById(1L);
        verify(taskMapper).toDomain(taskEntity);
    }

    @Test
    void shouldSuccessfullyChangeTaskStatusById() {
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-04-16T11:00:00Z");
        Task updatedDomain = new Task(1L, "test-task", "test-description", TaskStatus.DONE, taskEntity.getCreatedAt(), updatedAt);

        when(taskEntityMapper.findById(1L)).thenReturn(Optional.of(taskEntity));
        when(taskEntityMapper.updateStatus(eq(1L), eq(TaskStatus.DONE), any(OffsetDateTime.class))).thenReturn(1);
        when(taskMapper.toDomain(any(TaskEntity.class))).thenReturn(updatedDomain);

        Task updatedTask = taskService.changeTaskStatusById(1L, updateRequestDto);

        assertEquals(1L, updatedTask.id());
        assertEquals(TaskStatus.DONE, updatedTask.status());

        verify(taskEntityMapper).findById(1L);
        verify(taskEntityMapper).updateStatus(eq(1L), eq(TaskStatus.DONE), any(OffsetDateTime.class));
        verify(taskMapper).toDomain(any(TaskEntity.class));
    }

    @Test
    void shouldThrowIllegalTaskArgumentExceptionWhenCreateRequestIsNull() {
        IllegalTaskArgumentException exception = assertThrows(
                IllegalTaskArgumentException.class,
                () -> taskService.createTask(null)
        );

        assertEquals("Task create request is null", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalTaskArgumentExceptionWhenTaskIdIsNull() {
        IllegalTaskArgumentException exception = assertThrows(
                IllegalTaskArgumentException.class,
                () -> taskService.getTaskById(null)
        );

        assertEquals("Task id is null", exception.getMessage());
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenTaskNotFoundById() {
        when(taskEntityMapper.findById(999L)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(999L)
        );

        assertEquals("Task with id=999 not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalTaskArgumentExceptionWhenTaskIdOrUpdateRequestIsNull() {
        IllegalTaskArgumentException exceptionForNullId = assertThrows(
                IllegalTaskArgumentException.class,
                () -> taskService.changeTaskStatusById(null, updateRequestDto)
        );
        assertEquals("Task id or request is null", exceptionForNullId.getMessage());

        IllegalTaskArgumentException exceptionForNullRequest = assertThrows(
                IllegalTaskArgumentException.class,
                () -> taskService.changeTaskStatusById(1L, null)
        );
        assertEquals("Task id or request is null", exceptionForNullRequest.getMessage());
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenChangingStatusForNotExistingTask() {
        when(taskEntityMapper.findById(999L)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.changeTaskStatusById(999L, updateRequestDto)
        );

        assertEquals("Task with id=999 not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalTaskStateExceptionWhenUpdatedRowsCountIsNotOne() {
        when(taskEntityMapper.findById(1L)).thenReturn(Optional.of(taskEntity));
        when(taskEntityMapper.updateStatus(eq(1L), eq(TaskStatus.DONE), any(OffsetDateTime.class))).thenReturn(0);

        IllegalTaskStateException exception = assertThrows(
                IllegalTaskStateException.class,
                () -> taskService.changeTaskStatusById(1L, updateRequestDto)
        );

        assertEquals("Failed to update status for task with id=1", exception.getMessage());
    }
}