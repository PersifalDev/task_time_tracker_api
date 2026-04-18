package ru.haritonenko.task_time_tracker_api.employee.domain.db.mapper;

import org.apache.ibatis.annotations.*;
import ru.haritonenko.task_time_tracker_api.employee.domain.db.entity.EmployeeEntity;

import java.util.Optional;

@Mapper
public interface EmployeeEntityMapper {

    @Insert("""
            INSERT INTO employees(login, password, age, employee_role, created_at, updated_at)
            VALUES (#{login}, #{password}, #{age}, #{employeeRole}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(EmployeeEntity employeeEntity);

    @Select("""
            SELECT id, login, password, age, employee_role, created_at, updated_at
            FROM employees
            WHERE id = #{id}
            """)
    Optional<EmployeeEntity> findById(@Param("id") Long id);

    @Select("""
            SELECT id, login, password, age, employee_role, created_at, updated_at
            FROM employees
            WHERE login = #{login}
            """)
    Optional<EmployeeEntity> findByLogin(@Param("login") String login);

    @Select("""
            SELECT EXISTS(
                SELECT 1
                FROM employees
                WHERE login = #{login}
            )
            """)
    boolean existsByLogin(@Param("login") String login);
}