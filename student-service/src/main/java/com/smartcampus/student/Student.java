package com.smartcampus.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Student {
    @Id
    private String id;
    private String name;
    private String programme;
    private double gpa;

    // Constructors, Getters, and Setters
    public Student() {}
    public Student(String id, String name, String programme, double gpa) {
        this.id = id;
        this.name = name;
        this.programme = programme;
        this.gpa = gpa;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
    public double getGPA() { return gpa; }
    public void setGPA(double gpa) { this.gpa = gpa; }
}