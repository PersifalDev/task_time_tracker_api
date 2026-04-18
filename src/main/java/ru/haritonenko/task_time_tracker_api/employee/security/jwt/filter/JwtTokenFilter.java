package ru.haritonenko.task_time_tracker_api.employee.security.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification.AuthEmployee;
import ru.haritonenko.task_time_tracker_api.employee.security.jwt.manager.JwtTokenManager;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (isNull(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authorizationHeader.substring(7);

        AuthEmployee employee;
        try {
            employee = jwtTokenManager.getAuthEmployeeFromToken(jwt);
        } catch (Exception ex) {
            log.warn("Error while reading jwt", ex);
            filterChain.doFilter(request, response);
            return;
        }

        if (isNull(employee) || isNull(employee.id()) || isNull(employee.role())) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                employee,
                null,
                List.of(new SimpleGrantedAuthority(employee.role()))
        );

        SecurityContextHolder.getContext().setAuthentication(token);
        filterChain.doFilter(request, response);
    }
}