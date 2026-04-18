package ru.haritonenko.task_time_tracker_api.tasks.domain.db.mapper;

import org.apache.ibatis.annotations.*;
import ru.haritonenko.task_time_tracker_api.tasks.domain.db.entity.TaskEntity;
import ru.haritonenko.task_time_tracker_api.tasks.domain.status.TaskStatus;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper
public interface TaskEntityMapper {

    @Insert("""
            INSERT INTO tasks(title, description, status, created_at, updated_at)
            VALUES (#{title}, #{description}, #{status}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TaskEntity taskEntity);

    @Select("""
            SELECT *
            FROM tasks
            WHERE id = #{id}
            """)
    Optional<TaskEntity> findById(@Param("id") Long id);

    @Update("""
            UPDATE tasks
            SET status = #{status},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") TaskStatus status,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
