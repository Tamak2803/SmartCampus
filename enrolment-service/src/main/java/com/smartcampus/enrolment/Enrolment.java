package com.smartcampus.enrolment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Enrolment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String studentId;
    private String courseCode;
    private String status;

    public Enrolment() {}
    public Enrolment(String studentId, String courseCode, String status) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.status = status;
    }
    public Long getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getCourseCode() { return courseCode; }
    public String getStatus() { return status; }
}