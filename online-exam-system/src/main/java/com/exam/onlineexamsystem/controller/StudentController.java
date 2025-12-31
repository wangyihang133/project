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

    // 按 applicationId 分组返回当前学生所有成绩
    @GetMapping("/scores/by-application")
    public ResponseEntity<?> myScoresGroupedByApplication(
            @RequestHeader(value = "Authorization", required = false) String auth) {

        AuthContext ctx = authService.fromAuthHeader(auth);
        if (ctx == null) return ResponseEntity.status(401).body("UNAUTHORIZED");

        Integer studentId = ensureStudent(ctx.getUserId(), null);

        // 把 application + exam 信息一起带出来，前端展示会很方便
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT " +
                        "a.id AS application_id, a.status AS application_status, a.application_time, " +
                        "e.id AS exam_id, e.exam_name, e.exam_type, e.exam_time, e.exam_major, " +
                        "s.id AS score_id, s.subject, s.score, s.entry_time " +
                "FROM applications a " +
                "LEFT JOIN exams e ON a.exam_id = e.id " +
                "LEFT JOIN scores s ON s.application_id = a.id " +
                "WHERE a.student_id = ? " +
                "ORDER BY a.application_time DESC, s.entry_time ASC",
                studentId
        );

        // 分组：applicationId -> { application信息 + scores[] + total }
        Map<Integer, Map<String, Object>> grouped = new LinkedHashMap<>();

        for (Map<String, Object> r : rows) {
            Integer appId = r.get("application_id") == null ? null : ((Number) r.get("application_id")).intValue();
            if (appId == null) continue;

            Map<String, Object> app = grouped.get(appId);
            if (app == null) {
                app = new LinkedHashMap<>();
                app.put("applicationId", appId);
                app.put("applicationStatus", r.get("application_status"));
                app.put("applicationTime", r.get("application_time"));

                app.put("examId", r.get("exam_id"));
                app.put("examName", r.get("exam_name"));
                app.put("examType", r.get("exam_type"));
                app.put("examTime", r.get("exam_time"));
                app.put("examMajor", r.get("exam_major"));

                app.put("scores", new ArrayList<Map<String, Object>>());
                app.put("total", 0.0);

                grouped.put(appId, app);
            }

            // scores 可能为空（没录入成绩时 LEFT JOIN 为 null）
            Object scoreIdObj = r.get("score_id");
            if (scoreIdObj != null) {
                Map<String, Object> score = new LinkedHashMap<>();
                score.put("id", ((Number) scoreIdObj).intValue());
                score.put("subject", r.get("subject"));
                score.put("score", r.get("score"));
                score.put("entryTime", r.get("entry_time"));

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> scores = (List<Map<String, Object>>) app.get("scores");
                scores.add(score);

                Object sc = r.get("score");
                if (sc instanceof Number) {
                    double total = (double) app.get("total");
                    total += ((Number) sc).doubleValue();
                    app.put("total", total);
                }
            }
        }

        // 返回数组，前端更好循环
        return ResponseEntity.ok(new ArrayList<>(grouped.values()));
    }

    // 通过 username 查 student -> applications -> scores，按 applicationId 分组返回
    @GetMapping("/scores/by-application-username")
    public ResponseEntity<?> scoresByApplicationUsername(
            @RequestParam("username") String username
    ) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("USERNAME_REQUIRED");
        }
        username = username.trim();

        // 1) username -> user_id
        Integer userId;
        try {
            userId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE username = ?",
                    Integer.class,
                    username
            );
        } catch (Exception e) {
            return ResponseEntity.status(404).body("USER_NOT_FOUND");
        }

        // 2) user_id -> student_id
        Integer studentId;
        try {
            studentId = jdbcTemplate.queryForObject(
                    "SELECT id FROM students WHERE user_id = ?",
                    Integer.class,
                    userId
            );
        } catch (Exception e) {
            // 没有学生记录，返回空数组
            return ResponseEntity.ok(Collections.emptyList());
        }

        // 3) 查 application + exam + student + scores（把 student.major 一起带出来）
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT " +
                "a.id AS application_id, a.status AS application_status, a.application_time, " +
                "e.id AS exam_id, e.exam_name, e.exam_type, e.exam_time, e.exam_major, " +
                "st.major AS student_major, " +
                "s.id AS score_id, s.subject, s.score, s.entry_time " +
            "FROM applications a " +
            "LEFT JOIN exams e ON a.exam_id = e.id " +
            "LEFT JOIN students st ON a.student_id = st.id " +
            "LEFT JOIN scores s ON s.application_id = a.id " +
            "WHERE a.student_id = ? " +
            "ORDER BY a.application_time DESC, s.entry_time ASC",
            studentId
        );

        Map<Integer, Map<String, Object>> grouped = new LinkedHashMap<>();

        for (Map<String, Object> r : rows) {
            Integer appId = r.get("application_id") == null ? null : ((Number) r.get("application_id")).intValue();
            if (appId == null) continue;

            Map<String, Object> app = grouped.get(appId);
            if (app == null) {
                app = new LinkedHashMap<>();
                app.put("applicationId", appId);
                app.put("applicationStatus", r.get("application_status"));
                app.put("applicationTime", r.get("application_time"));

                app.put("examId", r.get("exam_id"));
                app.put("examName", r.get("exam_name"));
                app.put("examType", r.get("exam_type"));
                app.put("examTime", r.get("exam_time"));
                app.put("examMajor", r.get("exam_major"));

                    // 保存 student_major 以便后续优先使用
                    app.put("studentMajor", r.get("student_major"));

                app.put("scores", new ArrayList<Map<String, Object>>());
                app.put("total", 0.0);

                grouped.put(appId, app);
            }

            Object scoreIdObj = r.get("score_id");
            if (scoreIdObj != null) {
                Map<String, Object> score = new LinkedHashMap<>();
                score.put("id", ((Number) scoreIdObj).intValue());
                score.put("subject", r.get("subject"));
                score.put("score", r.get("score"));
                score.put("entryTime", r.get("entry_time"));

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> scores = (List<Map<String, Object>>) app.get("scores");
                scores.add(score);

                Object sc = r.get("score");
                if (sc instanceof Number) {
                    double total = (double) app.get("total");
                    total += ((Number) sc).doubleValue();
                    app.put("total", total);
                }
            }
        }

        // 分组完成后，针对每个报名计算 minScore 与录取状态（优先 studentMajor，其次 examMajor）
        for (Map<String, Object> app : grouped.values()) {
            Object examIdObj = app.get("examId");
            String major = app.get("examMajor") == null ? null : app.get("examMajor").toString();

            if (app.get("studentMajor") != null && !app.get("studentMajor").toString().isBlank()) {
                major = app.get("studentMajor").toString();
            }

            Double minScore = null;
            if (examIdObj instanceof Number && major != null && !major.isBlank()) {
                Long examId = ((Number) examIdObj).longValue();
                try {
                    Map<String, Object> line = jdbcTemplate.queryForMap(
                            "SELECT min_score FROM admission_scoreline WHERE exam_id = ? AND major = ? ORDER BY set_time DESC LIMIT 1",
                            examId, major
                    );
                    Object ms = line.get("min_score");
                    if (ms instanceof Number) minScore = ((Number) ms).doubleValue();
                } catch (Exception ignored) {}
            }

            double total = app.get("total") instanceof Number ? ((Number) app.get("total")).doubleValue() : 0.0;

            String admissionStatus;
            if (minScore == null) admissionStatus = "待公布分数线";
            else admissionStatus = total >= minScore ? "已录取" : "未录取";

            app.put("minScore", minScore);
            app.put("admissionStatus", admissionStatus);
            // 将用于显示的专业也写入方便前端使用
            app.put("major", major);
        }

        return ResponseEntity.ok(new ArrayList<>(grouped.values()));
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