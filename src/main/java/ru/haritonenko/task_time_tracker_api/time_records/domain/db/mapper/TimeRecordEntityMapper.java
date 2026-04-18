package ru.haritonenko.task_time_tracker_api.time_records.domain.db.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import ru.haritonenko.task_time_tracker_api.time_records.domain.db.entity.TimeRecordEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TimeRecordEntityMapper {

    @Insert("""
            INSERT INTO time_records(employee_id, task_id, description, start_time, end_time, created_at, updated_at)
            VALUES (#{employeeId}, #{taskId}, #{description}, #{startTime}, #{endTime}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TimeRecordEntity timeRecordEntity);

    @Select("""
            SELECT *
            FROM time_records
            WHERE id = #{id}
            """)
    Optional<TimeRecordEntity> findById(@Param("id") Long id);

    @Select("""
            SELECT *
            FROM time_records
            WHERE employee_id = #{employeeId}
            ORDER BY start_time DESC
            LIMIT #{limit}
            OFFSET #{offset}
            """)
    List<TimeRecordEntity> findByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select("""
            SELECT *
            FROM time_records
            WHERE employee_id = #{employeeId}
              AND end_time >= #{startTime}
              AND start_time <= #{endTime}
            ORDER BY start_time DESC
            LIMIT #{limit}
            OFFSET #{offset}
            """)
    List<TimeRecordEntity> findByEmployeeIdAndPeriod(
            @Param("employeeId") Long employeeId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Select("""
            SELECT EXISTS(
                SELECT 1
                FROM time_records
                WHERE task_id = #{taskId}
            )
            """)
    boolean existsRecordForTask(@Param("taskId") Long taskId);
}