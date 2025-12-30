package com.exam.onlineexamsystem.dto;

public class ScoreEntryReq {
    private Integer applicationId;
    private String subject;
    private Double score;

    public Integer getApplicationId() { return applicationId; }
    public void setApplicationId(Integer applicationId) { this.applicationId = applicationId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}
