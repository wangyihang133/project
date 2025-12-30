package com.exam.onlineexamsystem.dto;

public class ScorelineReq {
    private Integer examYear;
    private String major;
    private Double minScore;

    public Integer getExamYear() { return examYear; }
    public void setExamYear(Integer examYear) { this.examYear = examYear; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public Double getMinScore() { return minScore; }
    public void setMinScore(Double minScore) { this.minScore = minScore; }
}
