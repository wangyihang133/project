package com.exam.onlineexamsystem.dto;

public class ScoreEntryReq {
    private Integer studentId;
    private String subject;
    private Double score;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer applicationId) { this.studentId = applicationId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}
