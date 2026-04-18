package ru.haritonenko.task_time_tracker_api.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI taskTimeTrackerOpenApi(
            @Value("${app.openapi.server-url}") String serverUrl
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("Сервис учета рабочего времени сотрудников")
                        .version("1.0.0")
                        .description("REST API для сотрудников, задач и учета затраченного времени")
                        .contact(new Contact().name("Task Time Tracker API")))
                .addServersItem(new Server().url(serverUrl).description("Основной сервер"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}
