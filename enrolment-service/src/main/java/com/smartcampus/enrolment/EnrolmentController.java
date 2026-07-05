package com.smartcampus.enrolment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/enrolments")
@CrossOrigin(origins = "*")
public class EnrolmentController {

    private final EnrolmentService enrolmentService;

    public EnrolmentController(EnrolmentService enrolmentService) {
        this.enrolmentService = enrolmentService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> register(
            @RequestParam("studentId") String studentId, 
            @RequestParam("courseCode") String courseCode) {
        return enrolmentService.requestEnrolment(studentId, courseCode)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return ResponseEntity.status(HttpStatus.CREATED).body(result.getMessage());
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMessage());
                    }
                });
    }
}