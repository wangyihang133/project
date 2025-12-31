package com.exam.onlineexamsystem.controller;

import com.exam.onlineexamsystem.dto.ConfirmReq;
import com.exam.onlineexamsystem.dto.RoomAssignReq;
import com.exam.onlineexamsystem.dto.ScoreEntryReq;
import com.exam.onlineexamsystem.dto.ScorelineReq;
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
        jdbcTemplate.update("INSERT INTO exam_info (title, content, publish_by, publish_time, is_active) VALUES (?, ?, ?, ?, ?)",
                title, content, ctx.getUsername(), now, isActive);
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

    // -------- application confirm --------

    @GetMapping("/applications")
    public ResponseEntity<?> listApplications(@RequestHeader(value = "Authorization", required = false) String auth,
                                              @RequestParam(value = "status", required = false) String status) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        String sql = "SELECT a.*, u.username, u.phone, u.email, s.major FROM applications a " +
                "JOIN students s ON a.student_id = s.id " +
                "JOIN users u ON s.user_id = u.id ";
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
        log(ctx.getUserId(), "现场确认: applicationId=" + id + ", status=" + status, request);
        return ResponseEntity.ok("OK");
    }

    // -------- scoreline --------

    @PostMapping("/scoreline")
    public ResponseEntity<?> setScoreline(@RequestHeader(value = "Authorization", required = false) String auth,
                                          @RequestBody ScorelineReq req,
                                          HttpServletRequest request) {
        AuthContext ctx = authService.fromAuthHeader(auth);
        if (!canRecruit(ctx)) return ResponseEntity.status(403).body("FORBIDDEN");

        if (req.getExamYear() == null || req.getMajor() == null || req.getMajor().isBlank() || req.getMinScore() == null) {
            return ResponseEntity.badRequest().body("INVALID_PAYLOAD");
        }

        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admission_scoreline WHERE exam_year = ? AND major = ?",
                Integer.class, req.getExamYear(), req.getMajor()
        );
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (cnt != null && cnt > 0) {
            jdbcTemplate.update("UPDATE admission_scoreline SET min_score = ?, set_by = ?, set_time = ? WHERE exam_year = ? AND major = ?",
                    req.getMinScore(), ctx.getUserId(), now, req.getExamYear(), req.getMajor());
        } else {
            jdbcTemplate.update("INSERT INTO admission_scoreline (exam_year, major, min_score, set_by, set_time) VALUES (?, ?, ?, ?, ?)",
                    req.getExamYear(), req.getMajor(), req.getMinScore(), ctx.getUserId(), now);
        }
        log(ctx.getUserId(), "设置分数线: year=" + req.getExamYear() + ", major=" + req.getMajor() + ", minScore=" + req.getMinScore(), request);
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

        // confirmed applications without room
        List<Map<String, Object>> apps = jdbcTemplate.queryForList(
                "SELECT a.id FROM applications a LEFT JOIN exam_rooms r ON a.id = r.application_id " +
                        "WHERE a.status = '已确认' AND r.id IS NULL ORDER BY a.application_time ASC LIMIT 1000"
        );

        int seat = 1;
        int assigned = 0;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (Map<String, Object> a : apps) {
            Integer appId = ((Number) a.get("id")).intValue();
            String roomNumber = prefix + roomNo;
            jdbcTemplate.update(
                    "INSERT INTO exam_rooms (application_id, room_number, seat_number, exam_date, exam_time, address, assigned_by, assigned_time) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    appId, roomNumber, seat, examDate, examTime, address, ctx.getUserId(), now
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

        if (req.getStudentId() == null || req.getSubject() == null || req.getSubject().isBlank() || req.getScore() == null) {
            return ResponseEntity.badRequest().body("INVALID_PAYLOAD");
        }

        jdbcTemplate.update(
                "INSERT INTO scores (student_id, subject, score, entry_by, entry_time) VALUES (?, ?, ?, ?, ?)",
                req.getStudentId(), req.getSubject(), req.getScore(), ctx.getUserId(), Timestamp.valueOf(LocalDateTime.now())
        );
        log(ctx.getUserId(), "录入成绩: appId=" + req.getStudentId() + ", subject=" + req.getSubject() + ", score=" + req.getScore(), request);
        return ResponseEntity.ok("OK");
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
