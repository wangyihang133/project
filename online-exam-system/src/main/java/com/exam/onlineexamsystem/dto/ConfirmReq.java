package com.exam.onlineexamsystem.dto;

public class ConfirmReq {
    private boolean approve;
    private String status; // optional override

    public boolean isApprove() { return approve; }
    public void setApprove(boolean approve) { this.approve = approve; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
