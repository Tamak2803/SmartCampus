package com.smartcampus.analytics;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final CourseStatsRepository repository;

    public AnalyticsController(CourseStatsRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/registrations-by-course")
    public List<CourseStats> getRegistrationsByCourse() {
        return repository.findAll();
    }
}