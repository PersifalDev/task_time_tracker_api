package ru.haritonenko.task_time_tracker_api.employee.security.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification.CustomAuthenticationEntryPoint;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.handler.CustomAccessDeniedHandler;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.service.CustomEmployeeDetailsService;
import ru.haritonenko.task_time_tracker_api.employee.security.jwt.filter.JwtTokenFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final CustomEmployeeDetailsService customEmployeeDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenFilter jwtTokenFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/openapi.yaml"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employee/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/employee/debug/auth").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/employee").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/employee/auth").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/tasks/{id}").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/tasks/{id}/status").hasAnyAuthority("ADMIN", "USER")

                        .requestMatchers(HttpMethod.GET, "/api/time-records/{id}").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/time-records/info").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/time-records/search").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/time-records").hasAnyAuthority("ADMIN", "USER")

                        .requestMatchers("/error", "/error/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(jwtTokenFilter, AnonymousAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customEmployeeDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
