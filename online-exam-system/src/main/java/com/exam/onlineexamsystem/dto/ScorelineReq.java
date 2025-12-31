package com.exam.onlineexamsystem.dto;

public class ScorelineReq {
    private Long examId;
    private String major;
    private Double minScore;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public Double getMinScore() { return minScore; }
    public void setMinScore(Double minScore) { this.minScore = minScore; }
}
