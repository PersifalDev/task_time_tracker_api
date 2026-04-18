package ru.haritonenko.task_time_tracker_api.employee.security.jwt.manager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.employee.security.custom.authentification.AuthEmployee;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenManager {

    @Value("${jwt.secret-key}")
    private String keyString;

    @Value("${jwt.lifetime}")
    private long expirationTime;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(keyString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long employeeId, String login, String role) {
        return Jwts
                .builder()
                .subject(login)
                .claim("employeeId", employeeId)
                .claim("role", role)
                .signWith(key)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public AuthEmployee getAuthEmployeeFromToken(String jwt) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        Long employeeId = claims.get("employeeId", Long.class);
        String login = claims.getSubject();
        String role = claims.get("role", String.class);

        return AuthEmployee.builder()
                .id(employeeId)
                .login(login)
                .role(role)
                .build();
    }
}