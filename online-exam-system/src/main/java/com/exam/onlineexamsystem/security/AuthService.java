package com.exam.onlineexamsystem.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthService {
    private final TokenStore tokenStore;
    private final JdbcTemplate jdbcTemplate;

    public AuthService(TokenStore tokenStore, JdbcTemplate jdbcTemplate) {
        this.tokenStore = tokenStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthContext fromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) return null;
        String username = tokenStore.getUsername(token);
        if (username == null) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT id, username, role FROM users WHERE username = ?",
                    username
            );
            Integer userId = ((Number) row.get("id")).intValue();
            String role = (String) row.get("role");
            return new AuthContext(userId, username, role);
        } catch (Exception e) {
            return null;
        }
    }

    public TokenStore getTokenStore() {
        return tokenStore;
    }
}
