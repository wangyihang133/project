package com.exam.onlineexamsystem.controller;

import com.exam.onlineexamsystem.dto.LoginReq;
import com.exam.onlineexamsystem.dto.LoginResp;
import com.exam.onlineexamsystem.dto.RegisterReq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public AuthController() {
        // 初始化一个默认管理员
        passwordStore.put("admin", "123456");
        roleStore.put("admin", "ADMIN");
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterReq req) {
        if (req.getUsername() == null || req.getUsername().isEmpty()) {
            return "用户名不能为空";
        }
        if (passwordStore.containsKey(req.getUsername())) {
            return "用户名已存在";
        }
        passwordStore.put(req.getUsername(), req.getPassword());
        roleStore.put(req.getUsername(), req.getRole() == null ? "STUDENT" : req.getRole());
        return "ok";
    }

    @PostMapping("/login")
    public Object login(@RequestBody LoginReq req) {
        String username = req.getUsername();
        String pw = passwordStore.get(username);
        if (pw == null || !pw.equals(req.getPassword())) {
            // 返回 boolean false，以兼容前端旧逻辑
            return false;
        }

        String token = UUID.randomUUID().toString();
        tokenStore.put(token, username);

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUsername(username);
        resp.setRole(roleStore.getOrDefault(username, "STUDENT"));
        return resp;
    }
}
