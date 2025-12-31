package com.exam.onlineexamsystem.dto;

public class ApplicationReq {
    private Long examId;
    private Integer examYear;
    private String examType;
    private String major;
    private String phone;
    private String email;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Integer getExamYear() { return examYear; }
    public void setExamYear(Integer examYear) { this.examYear = examYear; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
