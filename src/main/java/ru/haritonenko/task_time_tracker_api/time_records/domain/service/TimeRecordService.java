package ru.haritonenko.task_time_tracker_api.time_records.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.task_time_tracker_api.config.PageConfig;
import ru.haritonenko.task_time_tracker_api.config.properties.CacheProperties;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;
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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class TimeRecordService {

    private static final String CACHE_KEY_PREFIX = "time-record:";

    private final TimeRecordEntityMapper timeRecordEntityMapper;
    private final TimeRecordToDomainMapper timeRecordMapper;
    private final EmployeeEntityMapper employeeEntityMapper;
    private final TaskEntityMapper taskEntityMapper;
    private final PageConfig pageConfig;
    private final RedisTemplate<String, TimeRecord> redisTimeRecordTemplate;
    private final CacheProperties cacheProperties;

    public TimeRecordService(
            TimeRecordEntityMapper timeRecordEntityMapper,
            TimeRecordToDomainMapper timeRecordMapper,
            EmployeeEntityMapper employeeEntityMapper,
            TaskEntityMapper taskEntityMapper,
            PageConfig pageConfig,
            ObjectProvider<RedisTemplate<String, TimeRecord>> redisTimeRecordTemplateProvider,
            CacheProperties cacheProperties
    ) {
        this.timeRecordEntityMapper = timeRecordEntityMapper;
        this.timeRecordMapper = timeRecordMapper;
        this.employeeEntityMapper = employeeEntityMapper;
        this.taskEntityMapper = taskEntityMapper;
        this.pageConfig = pageConfig;
        this.redisTimeRecordTemplate = redisTimeRecordTemplateProvider.getIfAvailable();
        this.cacheProperties = cacheProperties;
    }

    @Transactional
    public TimeRecord createTimeRecord(
            TimeRecordCreateRequestDto createRequestDto,
            AuthEmployee authenticatedEmployee
    ) {
        if (isNull(createRequestDto)) {
            log.warn("Time record create request is null");
            throw new IllegalTimeRecordArgumentException("Time record create request is null");
        }

        checkAuthenticatedEmployeeIsAllowedToWorkWithEmployeeId(
                authenticatedEmployee,
                createRequestDto.employeeId()
        );

        checkCreateRequestDtoConstraintsAreValidOrThrow(createRequestDto);
        checkEmployeeExistsOrThrow(createRequestDto.employeeId());
        checkTaskExistsOrThrow(createRequestDto.taskId());
        checkTaskHasNoTimeRecordOrThrow(createRequestDto.taskId());

        log.info("Creating time record for employeeId={} and taskId={}",
                createRequestDto.employeeId(),
                createRequestDto.taskId());

        OffsetDateTime now = OffsetDateTime.now();

        TimeRecordEntity timeRecordEntity = TimeRecordEntity.builder()
                .employeeId(createRequestDto.employeeId())
                .taskId(createRequestDto.taskId())
                .description(createRequestDto.description())
                .startTime(createRequestDto.startTime())
                .endTime(createRequestDto.endTime())
                .createdAt(now)
                .updatedAt(now)
                .build();

        timeRecordEntityMapper.insert(timeRecordEntity);

        if (isNull(timeRecordEntity.getId())) {
            log.warn("Time record id was not saved after insert");
            throw new IllegalTimeRecordStateException("Time record id was not saved");
        }

        TimeRecord timeRecord = mapToDomain(timeRecordEntity);
        cacheTimeRecord(timeRecord);

        log.info("Time record created with id={}", timeRecordEntity.getId());
        return timeRecord;
    }

    @Transactional(readOnly = true)
    public TimeRecord getTimeRecordById(Long id, AuthEmployee authenticatedEmployee) {
        if (isNull(id)) {
            log.warn("Time record id is null");
            throw new IllegalTimeRecordArgumentException("Time record id is null");
        }

        String key = getCacheKey(id);
        log.info("Getting time record by id={} from cache", id);
        TimeRecord timeRecordFromCache = getTimeRecordFromCache(key);
        if (nonNull(timeRecordFromCache)) {
            checkAuthenticatedEmployeeIsAllowedToWorkWithEmployeeId(
                    authenticatedEmployee,
                    timeRecordFromCache.employeeId()
            );
            log.info("Time record with id={} was successfully found in cache", id);
            return timeRecordFromCache;
        }

        log.info("Getting time record by id={} from db", id);
        TimeRecordEntity foundTimeRecord = findTimeRecordById(id);

        checkAuthenticatedEmployeeIsAllowedToWorkWithEmployeeId(
                authenticatedEmployee,
                foundTimeRecord.getEmployeeId()
        );

        TimeRecord timeRecord = mapToDomain(foundTimeRecord);
        cacheTimeRecord(timeRecord);

        log.info("Time record with id={} was successfully found in db", foundTimeRecord.getId());
        return timeRecord;
    }

    @Transactional(readOnly = true)
    public List<TimeRecord> getTimeRecordsByEmployeeIdWithFilters(
            TimeRecordRequestFilterDto requestFilter,
            TimeRecordPageFilter pageFilter,
            AuthEmployee authenticatedEmployee
    ) {
        Long employeeId = validateRequestFilterAndExtractEmployeeId(requestFilter);

        checkAuthenticatedEmployeeIsAllowedToWorkWithEmployeeId(
                authenticatedEmployee,
                employeeId
        );
        checkEmployeeExistsOrThrow(employeeId);

        Integer pageNumber = isNull(pageFilter) || isNull(pageFilter.pageNumber())
                ? pageConfig.defaultPageNumber()
                : pageFilter.pageNumber();

        Integer pageSize = isNull(pageFilter) || isNull(pageFilter.pageSize())
                ? pageConfig.defaultPageSize()
                : pageFilter.pageSize();

        int offset = pageNumber * pageSize;

        log.info("Getting time records for employeeId={} with startTime={}, endTime={}, pageNumber={}, pageSize={}",
                employeeId,
                requestFilter.startTime(),
                requestFilter.endTime(),
                pageNumber,
                pageSize
        );

        List<TimeRecordEntity> foundTimeRecords;
        if (isNull(requestFilter.startTime()) && isNull(requestFilter.endTime())) {
            foundTimeRecords = timeRecordEntityMapper.findByEmployeeId(
                    employeeId,
                    pageSize,
                    offset
            );
        } else {
            foundTimeRecords = timeRecordEntityMapper.findByEmployeeIdAndPeriod(
                    employeeId,
                    requestFilter.startTime(),
                    requestFilter.endTime(),
                    pageSize,
                    offset
            );
        }

        log.info("Found {} time records for employeeId={}", foundTimeRecords.size(), employeeId);

        return foundTimeRecords.stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public TimeRecordInfo getTimeRecordInfoByEmployeeIdWithFilters(
            TimeRecordRequestFilterDto requestFilter,
            AuthEmployee authenticatedEmployee
    ) {
        Long employeeId = validateRequestFilterAndExtractEmployeeId(requestFilter);

        checkAuthenticatedEmployeeIsAllowedToWorkWithEmployeeId(
                authenticatedEmployee,
                employeeId
        );
        checkEmployeeExistsOrThrow(employeeId);

        OffsetDateTime startTime = requestFilter.startTime();
        OffsetDateTime endTime = requestFilter.endTime();

        log.info("Getting time record info for employeeId={} with startTime={}, endTime={}",
                employeeId,
                startTime,
                endTime
        );

        List<TimeRecordEntity> timeRecords;
        if (isNull(startTime) && isNull(endTime)) {
            timeRecords = timeRecordEntityMapper.findByEmployeeId(
                    employeeId,
                    Integer.MAX_VALUE,
                    0
            );
        } else {
            timeRecords = timeRecordEntityMapper.findByEmployeeIdAndPeriod(
                    employeeId,
                    startTime,
                    endTime,
                    Integer.MAX_VALUE,
                    0
            );
        }

        Set<Long> taskIds = timeRecords.stream()
                .map(TimeRecordEntity::getTaskId)
                .collect(Collectors.toSet());

        Map<Long, TaskEntity> tasksById = taskIds.stream()
                .map(taskEntityMapper::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(TaskEntity::getId, Function.identity()));

        Long totalTasksCount = (long) taskIds.size();

        Set<Long> doneTaskIds = taskIds.stream()
                .filter(taskId -> {
                    TaskEntity taskEntity = tasksById.get(taskId);
                    return !isNull(taskEntity) && TaskStatus.DONE.equals(taskEntity.getStatus());
                })
                .collect(Collectors.toSet());

        Long totalDoneTasksCount = (long) doneTaskIds.size();

        Long totalSpentMinutes = timeRecords.stream()
                .filter(timeRecord -> doneTaskIds.contains(timeRecord.getTaskId()))
                .mapToLong(timeRecord -> Duration.between(
                        timeRecord.getStartTime(),
                        timeRecord.getEndTime()
                ).toMinutes())
                .sum();

        log.info("Time record info for employeeId={} was successfully calculated", employeeId);

        return TimeRecordInfo.builder()
                .employeeId(employeeId)
                .totalTasksCount(totalTasksCount)
                .totalDoneTasksCount(totalDoneTasksCount)
                .totalSpentMinutes(totalSpentMinutes)
                .firstTimeBoundPeriod(startTime)
                .secondTimeBoundPeriod(endTime)
                .build();
    }

    private TimeRecordEntity findTimeRecordById(Long id) {
        return timeRecordEntityMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("Time record with id={} not found", id);
                    return new TimeRecordNotFoundException("Time record with id=%d not found".formatted(id));
                });
    }

    private TimeRecord mapToDomain(TimeRecordEntity timeRecordEntity) {
        return timeRecordMapper.toDomain(timeRecordEntity);
    }

    private void checkCreateRequestDtoConstraintsAreValidOrThrow(TimeRecordCreateRequestDto createRequestDto) {
        if (isNull(createRequestDto.employeeId())
                || isNull(createRequestDto.taskId())
                || isNull(createRequestDto.description())
                || isNull(createRequestDto.startTime())
                || isNull(createRequestDto.endTime())) {
            log.warn("One or more required fields for time record creation are null");
            throw new IllegalTimeRecordArgumentException("Required time record fields must not be null");
        }

        if (createRequestDto.endTime().isBefore(createRequestDto.startTime())) {
            log.warn("End time is before start time for employeeId={} and taskId={}",
                    createRequestDto.employeeId(),
                    createRequestDto.taskId());
            throw new IllegalTimeRecordArgumentException("End time must not be before start time");
        }
    }

    private Long validateRequestFilterAndExtractEmployeeId(TimeRecordRequestFilterDto requestFilter) {
        if (isNull(requestFilter)) {
            log.warn("Request filter is null");
            throw new IllegalTimeRecordArgumentException("Request filter is null");
        }

        Long employeeId = requestFilter.employeeId();

        if (isNull(employeeId)) {
            log.warn("Employee id is null");
            throw new IllegalTimeRecordArgumentException("Employee id is null");
        }

        if ((!isNull(requestFilter.startTime()) && isNull(requestFilter.endTime()))
                || (isNull(requestFilter.startTime()) && !isNull(requestFilter.endTime()))) {
            log.warn("Only one period border was passed for employeeId={}", employeeId);
            throw new IllegalTimeRecordArgumentException("Start time and end time must be filled together");
        }

        if (!isNull(requestFilter.startTime())
                && !isNull(requestFilter.endTime())
                && requestFilter.endTime().isBefore(requestFilter.startTime())) {
            log.warn("End time is before start time for employeeId={}", employeeId);
            throw new IllegalTimeRecordArgumentException("End time must not be before start time");
        }

        return employeeId;
    }

    private void checkAuthenticatedEmployeeIsAllowedToWorkWithEmployeeId(
            AuthEmployee authenticatedEmployee,
            Long requestedEmployeeId
    ) {
        if (isNull(authenticatedEmployee)
                || isNull(authenticatedEmployee.id())
                || isNull(authenticatedEmployee.role())) {
            log.warn("Authenticated employee is invalid");
            throw new AccessDeniedException("Access denied");
        }

        if ("ADMIN".equals(authenticatedEmployee.role())) {
            return;
        }

        if (!authenticatedEmployee.id().equals(requestedEmployeeId)) {
            log.warn(
                    "Employee id={} with role={} tried to access employeeId={}",
                    authenticatedEmployee.id(),
                    authenticatedEmployee.role(),
                    requestedEmployeeId
            );
            throw new AccessDeniedException("Access denied");
        }
    }

    private void checkEmployeeExistsOrThrow(Long employeeId) {
        boolean employeeExists = employeeEntityMapper.findById(employeeId).isPresent();

        if (!employeeExists) {
            log.warn("Employee with id={} not found", employeeId);
            throw new IllegalTimeRecordArgumentException(
                    "Employee with id=%d not found".formatted(employeeId)
            );
        }
    }

    private void checkTaskExistsOrThrow(Long taskId) {
        boolean taskExists = taskEntityMapper.findById(taskId).isPresent();

        if (!taskExists) {
            log.warn("Task with id={} not found", taskId);
            throw new IllegalTimeRecordArgumentException(
                    "Task with id=%d not found".formatted(taskId)
            );
        }
    }

    private void checkTaskHasNoTimeRecordOrThrow(Long taskId) {
        boolean occupied = timeRecordEntityMapper.existsRecordForTask(taskId);

        if (occupied) {
            log.warn("Task id={} already has a time record", taskId);
            throw new TaskOccupiedByAnotherEmployeeException(
                    "Task with id=%d already has a time record".formatted(taskId)
            );
        }
    }

    private TimeRecord getTimeRecordFromCache(String key) {
        if (isNull(redisTimeRecordTemplate)) {
            return null;
        }

        try {
            return redisTimeRecordTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during time record cache read, fallback to DB. key={}", key, ex);
            return null;
        }
    }

    private void cacheTimeRecord(TimeRecord timeRecord) {
        if (isNull(redisTimeRecordTemplate) || isNull(timeRecord) || isNull(timeRecord.id())) {
            return;
        }

        String key = getCacheKey(timeRecord.id());
        try {
            log.info("Saving time record by id={} in cache", timeRecord.id());
            redisTimeRecordTemplate.opsForValue().set(key, timeRecord, cacheProperties.timeRecordsTtl());
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during time record cache write. key={}", key, ex);
        }
    }

    private String getCacheKey(Long id) {
        return CACHE_KEY_PREFIX + id;
    }
}
