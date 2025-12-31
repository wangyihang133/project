package com.exam.onlineexamsystem.dto;

import java.time.LocalDateTime;

public class ExamDto {
    private Long id;
    private String examName;
    private String examType;
    private LocalDateTime examTime;
    private Integer candidateCount;
    private String examMajor;
    private String remarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public LocalDateTime getExamTime() { return examTime; }
    public void setExamTime(LocalDateTime examTime) { this.examTime = examTime; }

    public Integer getCandidateCount() { return candidateCount; }
    public void setCandidateCount(Integer candidateCount) { this.candidateCount = candidateCount; }

    public String getExamMajor() { return examMajor; }
    public void setExamMajor(String examMajor) { this.examMajor = examMajor; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
