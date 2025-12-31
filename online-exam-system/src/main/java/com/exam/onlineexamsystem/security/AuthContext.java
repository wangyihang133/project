package com.exam.onlineexamsystem.security;

public class AuthContext {
    private Integer userId;
    private String username;
    private String role;

    public AuthContext() {}

    public AuthContext(Integer userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /**
     * 系统管理员：system_admin
     */
    public boolean isSystemAdmin() {
        return role != null && "system_admin".equalsIgnoreCase(role.trim());
    }

    /**
     * 招生管理员：recruitment_admin
     */
    public boolean isRecruitAdmin() {
        return role != null && "recruitment_admin".equalsIgnoreCase(role.trim());
    }

    /**
     * 考生：student
     */
    public boolean isStudent() {
        return role != null && "student".equalsIgnoreCase(role.trim());
    }
}
