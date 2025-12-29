package com.exam.onlineexamsystem.dto;

import java.sql.Timestamp;

public class LoginHistoryDto {
    private int id;
    private Timestamp loginTime;
    private String ipAddress;
    private String userAgent;
    private String username; // optional, shown to admins

    public LoginHistoryDto(int id, Timestamp loginTime, String ipAddress, String userAgent, String username) {
        this.id = id;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.username = username;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getLoginTime() { return loginTime; }
    public void setLoginTime(Timestamp loginTime) { this.loginTime = loginTime; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
