package com.exam.onlineexamsystem.controller;

import com.exam.onlineexamsystem.dto.LoginReq;
import com.exam.onlineexamsystem.dto.LoginHistoryDto;
import com.exam.onlineexamsystem.dto.LoginResp;
import com.exam.onlineexamsystem.dto.RegisterReq;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    // 简单的内存用户存储（示例用途，生产请使用数据库和密码哈希）
    private final Map<String, String> passwordStore = new ConcurrentHashMap<>();
    private final Map<String, String> roleStore = new ConcurrentHashMap<>();
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // 初始化一个默认管理员
        // 保留内存 map 仅作兼容（可逐步移除）
        passwordStore.put("admin", "123456");
        roleStore.put("admin", "ADMIN");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterReq req) {
        if (req.getUsername() == null || req.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body("PASSWORD_TOO_SHORT");
        }
        try {
            Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM users WHERE username = ?", Integer.class, req.getUsername());
            if (cnt != null && cnt > 0) {
                return ResponseEntity.status(409).body("用户名已存在");
            }

            // server-side enforce new users as student (ignore client-provided role)
            String role = "student";

            jdbcTemplate.update("INSERT INTO users (username, password, role) VALUES (?, ?, ?)", req.getUsername(), req.getPassword(), role);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("注册失败");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req, HttpServletRequest request) {
        String username = req.getUsername();
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap("SELECT id, password, role FROM users WHERE username = ?", username);
            if (row == null || row.isEmpty()) {
                return ResponseEntity.status(404).body("USER_NOT_FOUND");
            }
            String dbPassword = (String) row.get("password");
            if (dbPassword == null || !dbPassword.equals(req.getPassword())) {
                return ResponseEntity.status(401).body("WRONG_PASSWORD");
            }

            String token = UUID.randomUUID().toString();
            tokenStore.put(token, username);

            // insert login history
            try {
                Integer userId = (Integer) row.get("id");
                String ip = request.getRemoteAddr();
                String ua = request.getHeader("User-Agent");
                jdbcTemplate.update("INSERT INTO login_history (user_id, ip_address, user_agent) VALUES (?, ?, ?)", userId, ip, ua);
            } catch (Exception ignored) {}

            LoginResp resp = new LoginResp();
            resp.setToken(token);
            resp.setUsername(username);
            String roleDb = (String) row.get("role");
            // map DB role back to frontend role
            String roleOut = "STUDENT";
            if (roleDb != null) {
                if (roleDb.toLowerCase().contains("admin")) roleOut = "ADMIN";
                else if (roleDb.toLowerCase().contains("recruit")) roleOut = "RECRUIT";
                else roleOut = "STUDENT";
            }
            resp.setRole(roleOut);
            return ResponseEntity.ok(resp);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            return ResponseEntity.status(404).body("USER_NOT_FOUND");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("LOGIN_ERROR");
        }
    }

    @GetMapping("/login-history")
    public List<LoginHistoryDto> getLoginHistory(@RequestHeader(value = "Authorization", required = false) String auth,
                                                 @RequestParam(value = "all", required = false, defaultValue = "false") boolean all,
                                                 @RequestParam(value = "userId", required = false) Integer userIdParam,
                                                 @RequestParam(value = "username", required = false) String usernameParam,
                                                 @RequestParam(value = "startTime", required = false) String startTime,
                                                 @RequestParam(value = "endTime", required = false) String endTime) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return List.of();
        }
        String token = auth.substring("Bearer ".length()).trim();
        String username = tokenStore.get(token);
        if (username == null) return List.of();

        try {
            // determine role from users table (prefer DB truth)
            String role = jdbcTemplate.queryForObject("SELECT role FROM users WHERE username = ?", String.class, username);
            boolean isAdmin = role != null && role.toLowerCase().contains("admin");

            // build time filters
            java.sql.Timestamp startTs = null;
            java.sql.Timestamp endTs = null;
            try {
                if (startTime != null && !startTime.isBlank()) {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(startTime);
                    startTs = java.sql.Timestamp.valueOf(ldt);
                }
                if (endTime != null && !endTime.isBlank()) {
                    java.time.LocalDateTime ldt2 = java.time.LocalDateTime.parse(endTime);
                    endTs = java.sql.Timestamp.valueOf(ldt2);
                }
            } catch (Exception ex) {
                // ignore parse errors and treat as no filter
            }

            if (isAdmin && all && (usernameParam == null || usernameParam.isBlank())) {
                // admin requested all records (with optional time filters)
                StringBuilder q = new StringBuilder("SELECT lh.id, lh.login_time, lh.ip_address, lh.user_agent, u.username FROM login_history lh JOIN users u ON lh.user_id = u.id");
                StringBuilder where = new StringBuilder();
                java.util.List<Object> params = new java.util.ArrayList<>();
                if (startTs != null) { where.append(where.length() == 0 ? " WHERE" : " AND"); where.append(" lh.login_time >= ?"); params.add(startTs); }
                if (endTs != null) { where.append(where.length() == 0 ? " WHERE" : " AND"); where.append(" lh.login_time <= ?"); params.add(endTs); }
                q.append(where).append(" ORDER BY lh.login_time DESC LIMIT 500");
                List<LoginHistoryDto> rows = jdbcTemplate.query(q.toString(), params.toArray(), (rs, rowNum) -> new LoginHistoryDto(
                        rs.getInt("id"),
                        rs.getTimestamp("login_time"),
                        rs.getString("ip_address"),
                        rs.getString("user_agent"),
                        rs.getString("username")
                ));
                return rows;
            }

            Integer userId = null;
            if (isAdmin && userIdParam != null) {
                userId = userIdParam;
            } else if (isAdmin && usernameParam != null && !usernameParam.isBlank()) {
                // admin filters by username
                userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Integer.class, usernameParam);
            } else {
                userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Integer.class, username);
            }

            StringBuilder q2 = new StringBuilder("SELECT lh.id, lh.login_time, lh.ip_address, lh.user_agent, u.username FROM login_history lh JOIN users u ON lh.user_id = u.id WHERE lh.user_id = ?");
            java.util.List<Object> p2 = new java.util.ArrayList<>();
            p2.add(userId);
            if (startTs != null) { q2.append(" AND lh.login_time >= ?"); p2.add(startTs); }
            if (endTs != null) { q2.append(" AND lh.login_time <= ?"); p2.add(endTs); }
            q2.append(" ORDER BY lh.login_time DESC LIMIT 500");
            List<LoginHistoryDto> rows = jdbcTemplate.query(q2.toString(), p2.toArray(), (rs, rowNum) -> new LoginHistoryDto(
                    rs.getInt("id"),
                    rs.getTimestamp("login_time"),
                    rs.getString("ip_address"),
                    rs.getString("user_agent"),
                    rs.getString("username")
            ));
            return rows;
        } catch (Exception e) {
            return List.of();
        }
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return "no token";
        }
        String token = auth.substring("Bearer ".length()).trim();
        if (tokenStore.remove(token) != null) {
            return "ok";
        }
        return "not found";
    }
}
