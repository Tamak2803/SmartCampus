package com.smartcampus.library;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "studentId", "bookId" })
@XmlRootElement(name = "BookLoanRequest", namespace = "http://smartcampus.com/library")
public class BookLoanRequest {

    @XmlElement(namespace = "http://smartcampus.com/library", required = true)
    private String studentId;

    @XmlElement(namespace = "http://smartcampus.com/library", required = true)
    private String bookId;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}