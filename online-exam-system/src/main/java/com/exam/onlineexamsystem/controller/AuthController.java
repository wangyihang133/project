package com.exam.onlineexamsystem.controller;

import com.exam.onlineexamsystem.dto.LoginReq;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    @PostMapping("/login")
    public boolean login(@RequestBody LoginReq req) {
        // 先写死一组账号密码，后面接数据库再改
        return "admin".equals(req.getUsername()) && "123456".equals(req.getPassword());
    }
}
