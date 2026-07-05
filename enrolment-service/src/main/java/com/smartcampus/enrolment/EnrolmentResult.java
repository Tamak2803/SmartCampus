package com.smartcampus.enrolment;

public class EnrolmentResult {
    private final boolean success;
    private final String message;
    public EnrolmentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}