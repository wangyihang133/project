package com.exam.onlineexamsystem.controller;

import com.exam.onlineexamsystem.dto.ApplicationReq;
import com.exam.onlineexamsystem.security.AuthContext;
import com.exam.onlineexamsystem.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final JdbcTemplate jdbcTemplate;
    private final AuthService authService;

    public StudentController(JdbcTemplate jdbcTemplate, AuthService authService) {
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

    @GetMapping("/exam-info")
    public ResponseEntity<?> listActiveExamInfo(@RequestHeader(value = "Authorization", required = false) String auth) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (ctx == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, title, content, publish_by, publish_time, is_active FROM exam_info WHERE is_active = 1 ORDER BY publish_time DESC"
        );
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/applications/me")
    public ResponseEntity<?> myApplications(@RequestHeader(value = "Authorization", required = false) String auth) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (ctx == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        Integer studentId = ensureStudent(ctx.getUserId(), null);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM applications WHERE student_id = ? ORDER BY application_time DESC",
                studentId
        );
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/applications")
    public ResponseEntity<?> apply(@RequestHeader(value = "Authorization", required = false) String auth,
                                   @RequestBody ApplicationReq req,
                                   HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (ctx == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        if (req.getExamYear() == null || req.getExamYear() < 2000 || req.getExamYear() > 2099) {
            return ResponseEntity.badRequest().body("INVALID_EXAM_YEAR");
        }
        if (req.getExamType() == null || req.getExamType().isBlank()) {
            return ResponseEntity.badRequest().body("EXAM_TYPE_REQUIRED");
        }
        if (req.getMajor() == null || req.getMajor().isBlank()) {
            return ResponseEntity.badRequest().body("MAJOR_REQUIRED");
        }

        // update contact info to users table (design uses users.phone/email)
        try {
            jdbcTemplate.update("UPDATE users SET phone = ?, email = ?, updated_at = ? WHERE id = ?",
                    blankToNull(req.getPhone()), blankToNull(req.getEmail()),
                    Timestamp.valueOf(LocalDateTime.now()), ctx.getUserId());
        } catch (Exception ignored) {}

        Integer studentId = ensureStudent(ctx.getUserId(), req.getMajor());

        // prevent duplicate apply for same year/type: allow at most one
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM applications WHERE student_id = ? AND exam_year = ? AND exam_type = ?",
                Integer.class, studentId, req.getExamYear(), req.getExamType()
        );
        if (cnt != null && cnt > 0) {
            return ResponseEntity.status(409).body("ALREADY_APPLIED");
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update(
                "INSERT INTO applications (student_id, exam_year, exam_type, application_time, status) VALUES (?, ?, ?, ?, ?)",
                studentId, req.getExamYear(), req.getExamType(), now, "待确认"
        );
        log(ctx.getUserId(), "学生提交报名: year=" + req.getExamYear() + ", type=" + req.getExamType() + ", major=" + req.getMajor(), request);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/scores/me")
    public ResponseEntity<?> myScores(@RequestHeader(value = "Authorization", required = false) String auth) {

        AuthContext ctx = authService.fromAuthHeader(auth);
        if (ctx == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        Integer studentId = ensureStudent(ctx.getUserId(), null);


        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, student_id, subject, score, entry_by, entry_time FROM scores WHERE student_id = ? ORDER BY id ASC",
                studentId);
        double total = 0;
        for (Map<String, Object> r : rows) {
            Object s = r.get("score");
            if (s instanceof Number) total += ((Number) s).doubleValue();
        }
        return ResponseEntity.ok(Map.of("applicationId", studentId, "scores", rows, "total", total));
    }

    @GetMapping("/admission/me")
    public ResponseEntity<?> myAdmission(@RequestHeader(value = "Authorization", required = false) String auth) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (ctx == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        Integer studentId = ensureStudent(ctx.getUserId(), null);
        Map<String, Object> app;
        try {
            app = jdbcTemplate.queryForMap(
                    "SELECT a.*, s.major FROM applications a JOIN students s ON a.student_id = s.id WHERE a.student_id = ? ORDER BY a.application_time DESC LIMIT 1",
                    studentId
            );
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "未报名"));
        }

        Integer appId = ((Number) app.get("id")).intValue();
        Integer examYear = (app.get("exam_year") instanceof Number) ? ((Number) app.get("exam_year")).intValue() : null;
        String major = (String) app.get("major");

        double total = 0;
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT score FROM scores WHERE application_id = ?", appId);
            for (Map<String, Object> r : rows) {
                Object s = r.get("score");
                if (s instanceof Number) total += ((Number) s).doubleValue();
            }
        } catch (Exception ignored) {}

        Double minScore = null;
        try {
            Map<String, Object> line = jdbcTemplate.queryForMap(
                    "SELECT min_score FROM admission_scoreline WHERE exam_year = ? AND major = ? ORDER BY set_time DESC LIMIT 1",
                    examYear, major
            );
            Object ms = line.get("min_score");
            if (ms instanceof Number) minScore = ((Number) ms).doubleValue();
        } catch (Exception ignored) {}

        String status;
        if (minScore == null) status = "待公布分数线";
        else status = total >= minScore ? "已录取" : "未录取";

        return ResponseEntity.ok(Map.of(
                "applicationId", appId,
                "examYear", examYear,
                "major", major,
                "total", total,
                "minScore", minScore,
                "status", status
        ));
    }

    private Integer ensureStudent(Integer userId, String majorIfMissing) {
        try {
            Integer sid = jdbcTemplate.queryForObject("SELECT id FROM students WHERE user_id = ?", Integer.class, userId);
            if (sid != null) {
                if (majorIfMissing != null && !majorIfMissing.isBlank()) {
                    try { jdbcTemplate.update("UPDATE students SET major = ? WHERE id = ?", majorIfMissing, sid); } catch (Exception ignored) {}
                }
                return sid;
            }
        } catch (Exception ignored) {}

        // create minimal student row
        jdbcTemplate.update(
                "INSERT INTO students (user_id, major) VALUES (?, ?)",
                userId, majorIfMissing == null ? "" : majorIfMissing
        );
        return jdbcTemplate.queryForObject("SELECT id FROM students WHERE user_id = ?", Integer.class, userId);
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
