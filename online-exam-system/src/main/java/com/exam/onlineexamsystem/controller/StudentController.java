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

    // 学生端考试筛选列表（基于 exams 表）
    @GetMapping("/exams")
    public ResponseEntity<?> listExams(@RequestHeader(value = "Authorization", required = false) String auth,
                                       @RequestParam(value = "year", required = false) Integer year,
                                       @RequestParam(value = "type", required = false) String type,
                                       @RequestParam(value = "major", required = false) String major) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (ctx == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        StringBuilder sql = new StringBuilder("SELECT id, exam_name, exam_type, exam_time, exam_major, candidate_count, remarks FROM exams WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (year != null) {
            sql.append(" AND YEAR(exam_time) = ?");
            params.add(year);
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND exam_type = ?");
            params.add(type.trim());
        }
        if (major != null && !major.isBlank()) {
            sql.append(" AND exam_major LIKE ?");
            params.add("%" + major.trim() + "%");
        }

        sql.append(" ORDER BY exam_time ASC");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
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

        String major = req.getMajor();

        // 现在必须通过 examId 选择考试
        if (req.getExamId() == null) {
            return ResponseEntity.badRequest().body("EXAM_ID_REQUIRED");
        }

        // 以 exams 表为准填充 major
        Map<String, Object> exam;
        try {
            exam = jdbcTemplate.queryForMap("SELECT exam_major FROM exams WHERE id = ?", req.getExamId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("EXAM_NOT_FOUND");
        }
        if (exam.get("exam_major") != null) {
            major = exam.get("exam_major").toString();
        }

        if (major == null || major.isBlank()) {
            return ResponseEntity.badRequest().body("MAJOR_REQUIRED");
        }

        Integer studentId = ensureStudent(ctx.getUserId(), major);

        // prevent duplicate apply for same exam: allow at most one
        Integer cnt = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM applications WHERE student_id = ? AND exam_id = ?",
            Integer.class, studentId, req.getExamId()
        );
        if (cnt != null && cnt > 0) {
            return ResponseEntity.status(409).body("ALREADY_APPLIED");
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update(
            "INSERT INTO applications (student_id, exam_id, application_time, status) VALUES (?, ?, ?, ?)",
            studentId, req.getExamId(), now, "待确认"
        );
            log(ctx.getUserId(), "学生提交报名: examId=" + req.getExamId() + ", major=" + major, request);
        return ResponseEntity.ok("OK");
    }

    // 成绩查询：按报名ID + 考试条件 + 时间筛选
    @GetMapping("/scores/query")
    public ResponseEntity<?> queryScores(@RequestHeader(value = "Authorization", required = false) String auth,
                                         @RequestParam(value = "applicationId", required = false) Integer applicationId,
                                         @RequestParam(value = "examYear", required = false) Integer examYear,
                                         @RequestParam(value = "examType", required = false) String examType,
                                         @RequestParam(value = "fromDate", required = false) String fromDate,
                                         @RequestParam(value = "toDate", required = false) String toDate) {

        AuthContext ctx = authService.fromAuthHeader(auth);
        if (ctx == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        Integer studentId = ensureStudent(ctx.getUserId(), null);

        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.application_id, s.subject, s.score, s.entry_time, e.exam_time, e.exam_type " +
                    "FROM scores s JOIN applications a ON s.application_id = a.id " +
                    "LEFT JOIN exams e ON a.exam_id = e.id WHERE a.student_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(studentId);

        if (applicationId != null) {
            sql.append(" AND a.id = ?");
            params.add(applicationId);
        }
        if (examYear != null) {
            sql.append(" AND YEAR(e.exam_time) = ?");
            params.add(examYear);
        }
        if (examType != null && !examType.isBlank()) {
            sql.append(" AND e.exam_type = ?");
            params.add(examType.trim());
        }

        Timestamp fromTs = null;
        Timestamp toTs = null;
        try {
            if (fromDate != null && !fromDate.isBlank()) {
                // 支持 yyyy-MM-dd 或 yyyy-MM-ddTHH:mm 格式
                String d = fromDate.trim();
                if (d.length() == 10) d = d + " 00:00:00"; // 仅日期
                d = d.replace('T', ' ');
                fromTs = Timestamp.valueOf(d);
                sql.append(" AND s.entry_time >= ?");
                params.add(fromTs);
            }
            if (toDate != null && !toDate.isBlank()) {
                String d = toDate.trim();
                if (d.length() == 10) d = d + " 23:59:59";
                d = d.replace('T', ' ');
                toTs = Timestamp.valueOf(d);
                sql.append(" AND s.entry_time <= ?");
                params.add(toTs);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("INVALID_DATE_RANGE");
        }

        sql.append(" ORDER BY s.entry_time ASC");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        double total = 0;
        Integer usedAppId = null;
        for (Map<String, Object> r : rows) {
            Object sc = r.get("score");
            if (sc instanceof Number) total += ((Number) sc).doubleValue();
            if (usedAppId == null && r.get("application_id") instanceof Number) {
                usedAppId = ((Number) r.get("application_id")).intValue();
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("applicationId", usedAppId != null ? usedAppId : applicationId);
        resp.put("scores", rows);
        resp.put("total", total);
        return ResponseEntity.ok(resp);
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
                    "SELECT a.*, s.major, e.exam_time FROM applications a " +
                        "JOIN students s ON a.student_id = s.id " +
                        "LEFT JOIN exams e ON a.exam_id = e.id " +
                        "WHERE a.student_id = ? ORDER BY a.application_time DESC LIMIT 1",
                    studentId
            );
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "未报名"));
        }

        Integer appId = ((Number) app.get("id")).intValue();
        Integer examYear = null;
        Object et = app.get("exam_time");
        if (et instanceof java.sql.Timestamp ts) {
            examYear = ts.toLocalDateTime().getYear();
        }
        Long examId = null;
        Object eid = app.get("exam_id");
        if (eid instanceof Number) {
            examId = ((Number) eid).longValue();
        }
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
            if (examId != null) {
                Map<String, Object> line = jdbcTemplate.queryForMap(
					"SELECT min_score FROM admission_scoreline WHERE exam_id = ? AND major = ? ORDER BY set_time DESC LIMIT 1",
					examId, major
				);
                Object ms = line.get("min_score");
                if (ms instanceof Number) minScore = ((Number) ms).doubleValue();
            }
        } catch (Exception ignored) {}

        String status;
        if (minScore == null) status = "待公布分数线";
        else status = total >= minScore ? "已录取" : "未录取";

        return ResponseEntity.ok(Map.of(
                "applicationId", appId,
            "examId", examId,
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
