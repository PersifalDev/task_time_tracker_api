package ru.haritonenko.task_time_tracker_api.employee.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;

@Schema(description = "Информация о текущей аутентификации")
public record EmployeeAuthDebugResponse(
        @Schema(description = "Имя текущего пользователя", example = "admin")
        String name,
        @Schema(description = "Выданные роли и права")
        Collection<String> authorities
) {
}
