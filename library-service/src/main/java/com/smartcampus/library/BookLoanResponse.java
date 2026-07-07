package com.smartcampus.library;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "loanId", "status", "dueDate" })
@XmlRootElement(name = "BookLoanResponse", namespace = "http://smartcampus.com/library")
public class BookLoanResponse {

    @XmlElement(namespace = "http://smartcampus.com/library", required = true)
    private String loanId;

    @XmlElement(namespace = "http://smartcampus.com/library", required = true)
    private String status;

    @XmlElement(namespace = "http://smartcampus.com/library", required = true)
    private String dueDate;

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}