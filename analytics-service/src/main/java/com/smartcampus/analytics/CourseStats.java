package com.smartcampus.analytics;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CourseStats {
    @Id
    private String courseCode;
    private long totalRegistrations;

    public CourseStats() {}
    public CourseStats(String courseCode, long totalRegistrations) {
        this.courseCode = courseCode;
        this.totalRegistrations = totalRegistrations;
    }
    public String getCourseCode() { return courseCode; }
    public long getTotalRegistrations() { return totalRegistrations; }
    public void setTotalRegistrations(long totalRegistrations) { this.totalRegistrations = totalRegistrations; }
}