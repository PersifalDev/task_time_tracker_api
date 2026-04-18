package ru.haritonenko.task_time_tracker_api.tasks.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class TaskService {

    private static final String CACHE_KEY_PREFIX = "task:";

    private final TaskEntityMapper taskEntityMapper;
    private final TaskToDomainMapper taskMapper;
    private final RedisTemplate<String, Task> redisTaskTemplate;
    private final CacheProperties cacheProperties;

    public TaskService(
            TaskEntityMapper taskEntityMapper,
            TaskToDomainMapper taskMapper,
            ObjectProvider<RedisTemplate<String, Task>> redisTaskTemplateProvider,
            CacheProperties cacheProperties
    ) {
        this.taskEntityMapper = taskEntityMapper;
        this.taskMapper = taskMapper;
        this.redisTaskTemplate = redisTaskTemplateProvider.getIfAvailable();
        this.cacheProperties = cacheProperties;
    }

    @Transactional
    public Task createTask(TaskCreateRequestDto createRequestDto) {
        if (isNull(createRequestDto)) {
            log.warn("Task create request is null");
            throw new IllegalTaskArgumentException("Task create request is null");
        }

        log.info("Creating task");

        OffsetDateTime now = OffsetDateTime.now();

        TaskEntity taskEntity = TaskEntity.builder()
                .title(createRequestDto.title())
                .description(createRequestDto.description())
                .status(TaskStatus.NEW)
                .createdAt(now)
                .updatedAt(now)
                .build();

        taskEntityMapper.insert(taskEntity);

        Task createdTask = mapToDomain(taskEntity);
        cacheTask(createdTask);

        log.info("Task created with id={}", taskEntity.getId());
        return createdTask;
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        if (isNull(id)) {
            log.warn("Task id is null");
            throw new IllegalTaskArgumentException("Task id is null");
        }

        String key = getCacheKey(id);
        log.info("Getting task by id={} from cache", id);
        Task taskFromCache = getTaskFromCache(key);
        if (nonNull(taskFromCache)) {
            log.info("Task with id={} was successfully found in cache", id);
            return taskFromCache;
        }

        log.info("Getting task by id={} from db", id);
        TaskEntity foundTask = findTaskById(id);
        Task task = mapToDomain(foundTask);
        cacheTask(task);
        log.info("Task with id={} was successfully found in db", foundTask.getId());

        return task;
    }

    @Transactional
    public Task changeTaskStatusById(Long id, TaskUpdateRequestDto updateRequestDto) {
        if (isNull(id) || isNull(updateRequestDto)) {
            log.warn("Task id or request is null");
            throw new IllegalTaskArgumentException("Task id or request is null");
        }

        log.info("Updating task status for id={}", id);

        TaskEntity existingTask = findTaskById(id);
        OffsetDateTime now = OffsetDateTime.now();

        int updatedRows = taskEntityMapper.updateStatus(
                existingTask.getId(),
                updateRequestDto.status(),
                now
        );

        if (updatedRows != 1) {
            log.warn("Unexpected number of updated rows for task id={}: {}", existingTask.getId(), updatedRows);
            throw new IllegalTaskStateException(
                    "Failed to update status for task with id=%d".formatted(existingTask.getId())
            );
        }

        TaskEntity updatedTask = existingTask.toBuilder()
                .status(updateRequestDto.status())
                .updatedAt(now)
                .build();

        Task task = mapToDomain(updatedTask);
        cacheTask(task);

        log.info("Status for task id={} was updated to={}", updatedTask.getId(), updatedTask.getStatus());
        return task;
    }

    private TaskEntity findTaskById(Long id) {
        return taskEntityMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task with id={} not found", id);
                    return new TaskNotFoundException("Task with id=%d not found".formatted(id));
                });
    }

    private Task mapToDomain(TaskEntity taskEntity) {
        return taskMapper.toDomain(taskEntity);
    }

    private Task getTaskFromCache(String key) {
        if (isNull(redisTaskTemplate)) {
            return null;
        }

        try {
            return redisTaskTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during task cache read, fallback to DB. key={}", key, ex);
            return null;
        }
    }

    private void cacheTask(Task task) {
        if (isNull(redisTaskTemplate) || isNull(task) || isNull(task.id())) {
            return;
        }

        String key = getCacheKey(task.id());
        try {
            log.info("Saving task by id={} in cache", task.id());
            redisTaskTemplate.opsForValue().set(key, task, cacheProperties.tasksTtl());
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during task cache write. key={}", key, ex);
        }
    }

    private String getCacheKey(Long id) {
        return CACHE_KEY_PREFIX + id;
    }
}
