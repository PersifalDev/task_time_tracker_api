package ru.haritonenko.task_time_tracker_api.employee.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.task_time_tracker_api.config.properties.CacheProperties;
import ru.haritonenko.task_time_tracker_api.employee.api.dto.registration.EmployeeRegistration;
import ru.haritonenko.task_time_tracker_api.employee.domain.Employee;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper.EmployeeEntityMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.EmployeeNotFoundException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeArgumentException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeStateException;
import ru.haritonenko.task_time_tracker_api.employee.domain.mapper.EmployeeToDomainMapper;
import ru.haritonenko.task_time_tracker_api.employee.domain.role.EmployeeRole;

import java.time.OffsetDateTime;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class EmployeeService {

    private static final String EMPLOYEE_BY_ID_CACHE_KEY_PREFIX = "employee:id:";
    private static final String EMPLOYEE_BY_LOGIN_CACHE_KEY_PREFIX = "employee:login:";

    private final EmployeeEntityMapper employeeEntityMapper;
    private final EmployeeToDomainMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Employee> redisEmployeeTemplate;
    private final CacheProperties cacheProperties;

    public EmployeeService(
            EmployeeEntityMapper employeeEntityMapper,
            EmployeeToDomainMapper mapper,
            PasswordEncoder passwordEncoder,
            ObjectProvider<RedisTemplate<String, Employee>> redisEmployeeTemplateProvider,
            CacheProperties cacheProperties
    ) {
        this.employeeEntityMapper = employeeEntityMapper;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.redisEmployeeTemplate = redisEmployeeTemplateProvider.getIfAvailable();
        this.cacheProperties = cacheProperties;
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        if (isNull(id)) {
            log.warn("Employee id is null");
            throw new IllegalEmployeeArgumentException("Employee id is null");
        }

        String key = getEmployeeByIdCacheKey(id);
        log.info("Getting employee by id={} from cache", id);
        Employee employeeFromCache = getEmployeeFromCache(key);
        if (nonNull(employeeFromCache)) {
            log.info("Employee with id={} was successfully found in cache", id);
            return employeeFromCache;
        }

        log.info("Getting employee by id={} from db", id);
        EmployeeEntity foundEmployee = findEmployeeById(id);
        Employee employee = mapToDomain(foundEmployee);
        cacheEmployee(employee);
        log.info("Employee with id={} was successfully found in db", foundEmployee.getId());

        return employee;
    }

    @Transactional
    public Employee register(EmployeeRegistration employeeFromRegistration) {
        if (isNull(employeeFromRegistration)) {
            log.warn("Employee registration request is null");
            throw new IllegalEmployeeArgumentException("Employee registration request is null");
        }

        log.info("Registering employee with login={}", employeeFromRegistration.login());

        if (employeeEntityMapper.existsByLogin(employeeFromRegistration.login())) {
            log.warn("Employee with login={} already exists", employeeFromRegistration.login());
            throw new IllegalEmployeeStateException(
                    "Employee with login=%s is already registered".formatted(employeeFromRegistration.login())
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .login(employeeFromRegistration.login())
                .password(passwordEncoder.encode(employeeFromRegistration.password()))
                .age(employeeFromRegistration.age())
                .employeeRole(EmployeeRole.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        employeeEntityMapper.insert(employeeEntity);

        if (isNull(employeeEntity.getId())) {
            log.warn("Employee id was not generated after insert");
            throw new IllegalEmployeeStateException("Employee id was not generated");
        }

        Employee employee = mapToDomain(employeeEntity);
        cacheEmployee(employee);

        log.info("Employee registered with id={}, login={}", employeeEntity.getId(), employeeEntity.getLogin());
        return employee;
    }

    @Transactional(readOnly = true)
    public Employee findByLogin(String login) {
        if (isNull(login) || login.isBlank()) {
            log.warn("Employee login is null or blank");
            throw new IllegalEmployeeArgumentException("Employee login is null or blank");
        }

        String key = getEmployeeByLoginCacheKey(login);
        log.info("Getting employee by login={} from cache", login);
        Employee employeeFromCache = getEmployeeFromCache(key);
        if (nonNull(employeeFromCache)) {
            log.info("Employee with login={} was successfully found in cache", login);
            return employeeFromCache;
        }

        log.info("Getting employee by login={} from db", login);
        EmployeeEntity foundEmployee = findEmployeeByLogin(login);
        Employee employee = mapToDomain(foundEmployee);
        cacheEmployee(employee);
        log.info("Employee with login={} was successfully found in db", foundEmployee.getLogin());

        return employee;
    }

    private EmployeeEntity findEmployeeById(Long id) {
        return employeeEntityMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employee with id={} not found", id);
                    return new EmployeeNotFoundException("Employee with id=%d not found".formatted(id));
                });
    }

    private EmployeeEntity findEmployeeByLogin(String login) {
        return employeeEntityMapper.findByLogin(login)
                .orElseThrow(() -> {
                    log.warn("Employee with login={} not found", login);
                    return new EmployeeNotFoundException("Employee with login=%s not found".formatted(login));
                });
    }

    private Employee mapToDomain(EmployeeEntity employeeEntity) {
        return mapper.toDomain(employeeEntity);
    }

    private Employee getEmployeeFromCache(String key) {
        if (isNull(redisEmployeeTemplate)) {
            return null;
        }

        try {
            return redisEmployeeTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during employee cache read, fallback to DB. key={}", key, ex);
            return null;
        }
    }

    private void cacheEmployee(Employee employee) {
        if (isNull(redisEmployeeTemplate) || isNull(employee) || isNull(employee.id())) {
            return;
        }

        try {
            log.info("Saving employee by id={} and login={} in cache", employee.id(), employee.login());
            redisEmployeeTemplate.opsForValue().set(
                    getEmployeeByIdCacheKey(employee.id()),
                    employee,
                    cacheProperties.employeesTtl()
            );
            redisEmployeeTemplate.opsForValue().set(
                    getEmployeeByLoginCacheKey(employee.login()),
                    employee,
                    cacheProperties.employeesTtl()
            );
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during employee cache write. employeeId={}", employee.id(), ex);
        }
    }

    private String getEmployeeByIdCacheKey(Long id) {
        return EMPLOYEE_BY_ID_CACHE_KEY_PREFIX + id;
    }

    private String getEmployeeByLoginCacheKey(String login) {
        return EMPLOYEE_BY_LOGIN_CACHE_KEY_PREFIX + login;
    }
}
