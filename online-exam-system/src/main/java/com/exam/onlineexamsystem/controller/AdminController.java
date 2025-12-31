package com.exam.onlineexamsystem.controller;

import com.exam.onlineexamsystem.dto.AdminUserCreateReq;
import com.exam.onlineexamsystem.security.AuthContext;
import com.exam.onlineexamsystem.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final JdbcTemplate jdbcTemplate;
    private final AuthService authService;

    public AdminController(JdbcTemplate jdbcTemplate, AuthService authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
    }

    private void log(Integer userId, String action, HttpServletRequest request) {
        try {
            String ip = request.getRemoteAddr();
            jdbcTemplate.update("INSERT INTO system_logs (user_id, action, log_time, ip_address) VALUES (?, ?, ?, ?)",
                    userId, action, Timestamp.valueOf(LocalDateTime.now()), ip);
        } catch (Exception ignored) {}
    }

    private boolean isAdmin(AuthContext ctx) {
        // 只有 system_admin 才能进入系统管理/数据库维护
        return ctx != null && ctx.isSystemAdmin();
    }

    @GetMapping("/applications")
    public ResponseEntity<?> listApplications(@RequestHeader(value = "Authorization", required = false) String auth,
                                              @RequestParam(value = "status", required = false) String status,
                                              @RequestParam(value = "examYear", required = false) Integer examYear,
                                              @RequestParam(value = "major", required = false) String major) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!isAdmin(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String sql = "SELECT a.*, u.username, u.real_name, u.phone, u.email, s.major, e.exam_time " +
            "FROM applications a " +
            "JOIN students s ON a.student_id = s.id " +
            "JOIN users u ON s.user_id = u.id " +
            "LEFT JOIN exams e ON a.exam_id = e.id ";
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) { appendWhere(where, "a.status = ?"); params.add(status); }
        if (examYear != null) { appendWhere(where, "YEAR(e.exam_time) = ?"); params.add(examYear); }
        if (major != null && !major.isBlank()) { appendWhere(where, "s.major = ?"); params.add(major); }
        sql += where.toString() + " ORDER BY a.application_time DESC LIMIT 1000";
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql, params.toArray()));
    }

    @GetMapping("/scores")
    public ResponseEntity<?> listScores(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @RequestParam(value = "examYear", required = false) Integer examYear,
                                        @RequestParam(value = "major", required = false) String major) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!isAdmin(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String sql = "SELECT sc.*, a.status, u.username, s.major, e.exam_time, e.exam_type " +
            "FROM scores sc " +
            "JOIN applications a ON sc.application_id = a.id " +
            "JOIN students s ON a.student_id = s.id " +
            "JOIN users u ON s.user_id = u.id " +
            "LEFT JOIN exams e ON a.exam_id = e.id ";
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (examYear != null) { appendWhere(where, "YEAR(e.exam_time) = ?"); params.add(examYear); }
        if (major != null && !major.isBlank()) { appendWhere(where, "s.major = ?"); params.add(major); }
        sql += where.toString() + " ORDER BY sc.entry_time DESC LIMIT 2000";
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql, params.toArray()));
    }

    @PostMapping("/users")
    public ResponseEntity<?> createAdminUser(@RequestHeader(value = "Authorization", required = false) String auth,
                                            @RequestBody AdminUserCreateReq req,
                                            HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!isAdmin(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        if (req.getUsername() == null || req.getUsername().isBlank()) return ResponseEntity.badRequest().body("USERNAME_REQUIRED");
        if (req.getPassword() == null || req.getPassword().length() < 6) return ResponseEntity.badRequest().body("PASSWORD_TOO_SHORT");
        if (req.getRole() == null || req.getRole().isBlank()) return ResponseEntity.badRequest().body("ROLE_REQUIRED");

        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, req.getUsername());
        if (cnt != null && cnt > 0) return ResponseEntity.status(409).body("USERNAME_EXISTS");

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO users (username, password, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                req.getUsername(), req.getPassword(), req.getRole(), now, now);
        log(ctx.getUserId(), "创建管理员账号: " + req.getUsername() + ", role=" + req.getRole(), request);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/db/backup")
    public ResponseEntity<?> backup(@RequestHeader(value = "Authorization", required = false) String auth,
                                    HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!isAdmin(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        Map<String, Object> dump = new LinkedHashMap<>();
        dump.put("users", jdbcTemplate.queryForList("SELECT * FROM users LIMIT 5000"));
        dump.put("students", jdbcTemplate.queryForList("SELECT * FROM students LIMIT 5000"));
        dump.put("applications", jdbcTemplate.queryForList("SELECT * FROM applications LIMIT 5000"));
        dump.put("scores", jdbcTemplate.queryForList("SELECT * FROM scores LIMIT 10000"));
        dump.put("exam_rooms", jdbcTemplate.queryForList("SELECT * FROM exam_rooms LIMIT 5000"));
        dump.put("exam_info", jdbcTemplate.queryForList("SELECT * FROM exam_info LIMIT 2000"));
        dump.put("admission_scoreline", jdbcTemplate.queryForList("SELECT * FROM admission_scoreline LIMIT 2000"));
        dump.put("system_logs", jdbcTemplate.queryForList("SELECT * FROM system_logs ORDER BY log_time DESC LIMIT 5000"));

        log(ctx.getUserId(), "数据库备份导出", request);
        return ResponseEntity.ok(dump);
    }

    @PostMapping("/db/cleanup")
    public ResponseEntity<?> cleanup(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @RequestBody(required = false) Map<String, Object> body,
                                     HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!isAdmin(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        int days = 90;
        try { if (body != null && body.get("days") != null) days = Integer.parseInt(body.get("days").toString()); } catch (Exception ignored) {}
        int deleted = 0;
        try {
            deleted = jdbcTemplate.update("DELETE FROM system_logs WHERE log_time < (NOW() - INTERVAL ? DAY)", days);
        } catch (Exception ignored) {}
        log(ctx.getUserId(), "日志清理: days=" + days + ", deleted=" + deleted, request);
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    private void appendWhere(StringBuilder where, String clause) {
        if (where.length() == 0) where.append("WHERE ").append(clause).append(" ");
        else where.append("AND ").append(clause).append(" ");
    }
}
