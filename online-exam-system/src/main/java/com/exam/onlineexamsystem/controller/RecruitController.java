package com.exam.onlineexamsystem.controller;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exam.onlineexamsystem.dto.ConfirmReq;
import com.exam.onlineexamsystem.dto.RoomAssignReq;
import com.exam.onlineexamsystem.dto.ScoreEntryReq;
import com.exam.onlineexamsystem.dto.ScorelineReq;
import com.exam.onlineexamsystem.security.AuthContext;
import com.exam.onlineexamsystem.security.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/recruit")
public class RecruitController {

    private final JdbcTemplate jdbcTemplate;
    private final AuthService authService;

    public RecruitController(JdbcTemplate jdbcTemplate, AuthService authService) {
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

    private boolean canRecruit(AuthContext ctx) {
        // 招生管理员可用；系统管理员（全局）也允许查看/操作招生业务
        return ctx != null && (ctx.isRecruitAdmin() || ctx.isSystemAdmin());
    }

    // -------- exam_info CRUD --------

    @GetMapping("/exam-info")
    public ResponseEntity<?> listExamInfo(@RequestHeader(value = "Authorization", required = false) String auth) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");
        return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT * FROM exam_info ORDER BY publish_time DESC"));
    }

    @PostMapping("/exam-info")
    public ResponseEntity<?> createExamInfo(@RequestHeader(value = "Authorization", required = false) String auth,
                                           @RequestBody Map<String, Object> body,
                                           HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String title = body.get("title") == null ? null : body.get("title").toString();
        String content = body.get("content") == null ? null : body.get("content").toString();
        Integer isActive = body.get("is_active") == null ? 1 : Integer.parseInt(body.get("is_active").toString());
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body("TITLE_REQUIRED");

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try {
            jdbcTemplate.update("INSERT INTO exam_info (title, content, publish_by, publish_time, is_active) VALUES (?, ?, ?, ?, ?)",
                    title, content, ctx.getUserId(), now, isActive);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("DATABASE_ERROR: " + e.getMessage());
        }
        log(ctx.getUserId(), "发布/新增招考信息: " + title, request);
        return ResponseEntity.ok("OK");
    }

    @PutMapping("/exam-info/{id}")
    public ResponseEntity<?> updateExamInfo(@RequestHeader(value = "Authorization", required = false) String auth,
                                           @PathVariable("id") Integer id,
                                           @RequestBody Map<String, Object> body,
                                           HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String title = body.get("title") == null ? null : body.get("title").toString();
        String content = body.get("content") == null ? null : body.get("content").toString();
        Integer isActive = body.get("is_active") == null ? null : Integer.parseInt(body.get("is_active").toString());
        jdbcTemplate.update("UPDATE exam_info SET title = COALESCE(?, title), content = COALESCE(?, content), is_active = COALESCE(?, is_active) WHERE id = ?",
                blankToNull(title), blankToNull(content), isActive, id);
        log(ctx.getUserId(), "修改招考信息: id=" + id, request);
        return ResponseEntity.ok("OK");
    }

    @DeleteMapping("/exam-info/{id}")
    public ResponseEntity<?> deleteExamInfo(@RequestHeader(value = "Authorization", required = false) String auth,
                                           @PathVariable("id") Integer id,
                                           HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");
        jdbcTemplate.update("DELETE FROM exam_info WHERE id = ?", id);
        log(ctx.getUserId(), "删除招考信息: id=" + id, request);
        return ResponseEntity.ok("OK");
    }

    // -------- exams CRUD（考试设置） --------

    @GetMapping("/exams")
    public ResponseEntity<?> listExams(@RequestHeader(value = "Authorization", required = false) String auth,
                                       @RequestParam(value = "year", required = false) Integer year,
                                       @RequestParam(value = "type", required = false) String type,
                                       @RequestParam(value = "major", required = false) String major) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

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

        sql.append(" ORDER BY exam_time DESC");
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql.toString(), params.toArray()));
    }

    @PostMapping("/exams")
    public ResponseEntity<?> createExam(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String examName = body.get("exam_name") == null ? null : body.get("exam_name").toString();
        String examType = body.get("exam_type") == null ? null : body.get("exam_type").toString();
        String examTimeStr = body.get("exam_time") == null ? null : body.get("exam_time").toString();
        String examMajor = body.get("exam_major") == null ? null : body.get("exam_major").toString();
        Integer candidateCount = body.get("candidate_count") == null ? 0 : Integer.parseInt(body.get("candidate_count").toString());
        String remarks = body.get("remarks") == null ? null : body.get("remarks").toString();

        if (examName == null || examName.isBlank() || examType == null || examType.isBlank() || examTimeStr == null || examTimeStr.isBlank()) {
            return ResponseEntity.badRequest().body("INVALID_PAYLOAD");
        }

        Timestamp examTime;
        try {
            // 兼容 "yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-ddTHH:mm:ss" 两种格式
            String normalized = examTimeStr.replace('T', ' ');
            examTime = Timestamp.valueOf(normalized);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("INVALID_EXAM_TIME");
        }

        jdbcTemplate.update(
                "INSERT INTO exams (exam_name, exam_type, exam_time, exam_major, candidate_count, remarks) VALUES (?, ?, ?, ?, ?, ?)",
                examName, examType, examTime, examMajor, candidateCount, remarks
        );
        log(ctx.getUserId(), "创建考试: " + examName, request);
        return ResponseEntity.ok("OK");
    }

    @PutMapping("/exams/{id}")
    public ResponseEntity<?> updateExam(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @PathVariable("id") Long id,
                                        @RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String examName = body.get("exam_name") == null ? null : body.get("exam_name").toString();
        String examType = body.get("exam_type") == null ? null : body.get("exam_type").toString();
        String examTimeStr = body.get("exam_time") == null ? null : body.get("exam_time").toString();
        String examMajor = body.get("exam_major") == null ? null : body.get("exam_major").toString();
        Integer candidateCount = body.get("candidate_count") == null ? null : Integer.parseInt(body.get("candidate_count").toString());
        String remarks = body.get("remarks") == null ? null : body.get("remarks").toString();

        Timestamp examTime = null;
        if (examTimeStr != null && !examTimeStr.isBlank()) {
            try {
                String normalized = examTimeStr.replace('T', ' ');
                examTime = Timestamp.valueOf(normalized);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("INVALID_EXAM_TIME");
            }
        }

        jdbcTemplate.update(
                "UPDATE exams SET exam_name = COALESCE(?, exam_name), exam_type = COALESCE(?, exam_type), exam_time = COALESCE(?, exam_time), " +
                        "exam_major = COALESCE(?, exam_major), candidate_count = COALESCE(?, candidate_count), remarks = COALESCE(?, remarks) WHERE id = ?",
                blankToNull(examName), blankToNull(examType), examTime,
                blankToNull(examMajor), candidateCount, blankToNull(remarks), id
        );
        log(ctx.getUserId(), "修改考试: id=" + id, request);
        return ResponseEntity.ok("OK");
    }

    @DeleteMapping("/exams/{id}")
    public ResponseEntity<?> deleteExam(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @PathVariable("id") Long id,
                                        HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");
        jdbcTemplate.update("DELETE FROM exams WHERE id = ?", id);
        log(ctx.getUserId(), "删除考试: id=" + id, request);
        return ResponseEntity.ok("OK");
    }

    // -------- application confirm --------

    @GetMapping("/applications")
    public ResponseEntity<?> listApplications(@RequestHeader(value = "Authorization", required = false) String auth,
                                              @RequestParam(value = "status", required = false) String status) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String sql = "SELECT a.*, u.username, u.phone, u.email, s.major, e.exam_name FROM applications a " +
            "JOIN students s ON a.student_id = s.id " +
            "JOIN users u ON s.user_id = u.id " +
            "LEFT JOIN exams e ON a.exam_id = e.id ";
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql += "WHERE a.status = ? ";
            params.add(status);
        }
        sql += "ORDER BY a.application_time DESC LIMIT 500";
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql, params.toArray()));
    }

    @PostMapping("/applications/{id}/confirm")
    public ResponseEntity<?> confirm(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @PathVariable("id") Integer id,
                                     @RequestBody ConfirmReq req,
                                     HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String status = req.getStatus();
        if (status == null || status.isBlank()) status = req.isApprove() ? "已确认" : "已驳回";
        jdbcTemplate.update("UPDATE applications SET status = ?, confirmation_time = ?, confirmed_by = ? WHERE id = ?",
                status, Timestamp.valueOf(LocalDateTime.now()), ctx.getUserId(), id);
        log(ctx.getUserId(), "报名审核: applicationId=" + id + ", status=" + status, request);
        return ResponseEntity.ok("OK");
    }

    // -------- scoreline --------

    @PostMapping("/scoreline")
    public ResponseEntity<?> setScoreline(@RequestHeader(value = "Authorization", required = false) String auth,
                                          @RequestBody ScorelineReq req,
                                          HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        if (req.getExamId() == null || req.getMajor() == null || req.getMajor().isBlank() || req.getMinScore() == null) {
            return ResponseEntity.badRequest().body("INVALID_PAYLOAD");
        }

        Integer cnt = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM admission_scoreline WHERE exam_id = ? AND major = ?",
            Integer.class, req.getExamId(), req.getMajor()
        );
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (cnt != null && cnt > 0) {
            jdbcTemplate.update("UPDATE admission_scoreline SET min_score = ?, set_by = ?, set_time = ? WHERE exam_id = ? AND major = ?",
                req.getMinScore(), ctx.getUserId(), now, req.getExamId(), req.getMajor());
        } else {
            jdbcTemplate.update("INSERT INTO admission_scoreline (exam_id, major, min_score, set_by, set_time) VALUES (?, ?, ?, ?, ?)",
                req.getExamId(), req.getMajor(), req.getMinScore(), ctx.getUserId(), now);
        }
        log(ctx.getUserId(), "设置分数线: examId=" + req.getExamId() + ", major=" + req.getMajor() + ", minScore=" + req.getMinScore(), request);
        return ResponseEntity.ok("OK");
    }

    // -------- password reset --------

    @PostMapping("/students/{userId}/reset-password")
    public ResponseEntity<?> resetPwd(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @PathVariable("userId") Integer userId,
                                     @RequestBody Map<String, Object> body,
                                     HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String newPwd = body.get("password") == null ? null : body.get("password").toString();
        if (newPwd == null || newPwd.length() < 6) return ResponseEntity.badRequest().body("PASSWORD_TOO_SHORT");

        jdbcTemplate.update("UPDATE users SET password = ?, updated_at = ? WHERE id = ?",
                newPwd, Timestamp.valueOf(LocalDateTime.now()), userId);
        log(ctx.getUserId(), "重置学生密码: userId=" + userId, request);
        return ResponseEntity.ok("OK");
    }

    // -------- room assign --------

    @PostMapping("/exam-rooms/assign")
    public ResponseEntity<?> assignRooms(@RequestHeader(value = "Authorization", required = false) String auth,
                                         @RequestBody RoomAssignReq req,
                                         HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        int seatsPerRoom = req.getSeatsPerRoom() == null || req.getSeatsPerRoom() <= 0 ? 30 : req.getSeatsPerRoom();
        String prefix = req.getRoomPrefix() == null ? "A" : req.getRoomPrefix();
        int roomNo = req.getStartRoom() == null ? 101 : req.getStartRoom();
        String address = req.getAddress() == null ? "" : req.getAddress();

        Date examDate = null;
        try { if (req.getExamDate() != null && !req.getExamDate().isBlank()) examDate = Date.valueOf(LocalDate.parse(req.getExamDate())); } catch (Exception ignored) {}
        String examTime = req.getExamTime();

        // confirmed applications without room, optionally filtered by exam
                StringBuilder sql = new StringBuilder(
			"SELECT a.id, a.exam_id FROM applications a LEFT JOIN exam_rooms r ON a.id = r.application_id " +
				"WHERE a.status = '已确认' AND r.id IS NULL");
                List<Object> params = new ArrayList<>();
                if (req.getExamId() != null) {
                        sql.append(" AND a.exam_id = ?");
                        params.add(req.getExamId());
                }
                sql.append(" ORDER BY a.application_time ASC LIMIT 1000");

                List<Map<String, Object>> apps = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        int seat = 1;
        int assigned = 0;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (Map<String, Object> a : apps) {
                Integer appId = ((Number) a.get("id")).intValue();
                Long examId = null;
                Object eid = a.get("exam_id");
                if (eid instanceof Number) {
                     examId = ((Number) eid).longValue();
                }
            String roomNumber = prefix + roomNo;
            jdbcTemplate.update(
				"INSERT INTO exam_rooms (application_id, exam_id, room_number, seat_number, exam_date, exam_time, address, assigned_by, assigned_time) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				appId, examId, roomNumber, seat, examDate, examTime, address, ctx.getUserId(), now
            );
            assigned++;
            seat++;
            if (seat > seatsPerRoom) {
                seat = 1;
                roomNo++;
            }
        }

        log(ctx.getUserId(), "考场分配: assigned=" + assigned, request);
        return ResponseEntity.ok(Map.of("assigned", assigned));
    }

    // -------- score entry --------

    @PostMapping("/scores/entry")
    public ResponseEntity<?> entryScore(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @RequestBody ScoreEntryReq req,
                                        HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        if (req.getApplicationId() == null || req.getSubject() == null || req.getSubject().isBlank() || req.getScore() == null) {
            return ResponseEntity.badRequest().body("INVALID_PAYLOAD");
        }
        jdbcTemplate.update(
            "INSERT INTO scores (application_id, subject, score, entry_by, entry_time) VALUES (?, ?, ?, ?, ?)",
            req.getApplicationId(), req.getSubject(), req.getScore(), ctx.getUserId(), Timestamp.valueOf(LocalDateTime.now())
        );
        log(ctx.getUserId(), "录入成绩: appId=" + req.getApplicationId() + ", subject=" + req.getSubject() + ", score=" + req.getScore(), request);
        return ResponseEntity.ok("OK");
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
