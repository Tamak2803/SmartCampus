package com.smartcampus.analytics;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsEngine {

    private final CourseStatsRepository repository;

    public AnalyticsEngine(CourseStatsRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = AnalyticsBrokerConfig.QUEUE_NAME)
    public void consumeEnrolmentMessage(String rawJson) {
        try {
            // Find course code from JSON payload
            String searchPattern = "\"courseCode\":\"";
            int idx = rawJson.indexOf(searchPattern);
            if (idx != -1) {
                int start = idx + searchPattern.length();
                int end = rawJson.indexOf("\"", start);
                String courseCode = rawJson.substring(start, end);

                CourseStats stats = repository.findById(courseCode)
                        .orElse(new CourseStats(courseCode, 0));
                
                stats.setTotalRegistrations(stats.getTotalRegistrations() + 1);
                repository.save(stats);
                
                System.out.println("[ANALYTICS ENGINE] Aggregated registration database entry updated. Course Code: " 
                    + courseCode + " | Current Total Registrations: " + stats.getTotalRegistrations());
            }
        } catch (Exception e) {
            System.err.println("Failed to update aggregate metrics: " + e.getMessage());
        }
    }
}