package com.smartcampus.enrolment;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class StudentClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PROFILE_SERVICE_URL = "http://localhost:8081/api/students/";

    public boolean isStudentEligible(String studentId) {
        try {
            // Synchronous GET verification RPC
            String url = PROFILE_SERVICE_URL + studentId;
            var response = restTemplate.getForEntity(url, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (ResourceAccessException e) {
            // R9: Implement Fallback / Graceful degradation logic if target service is down
            System.err.println("[FALLBACK] Student Profile Service is down. Graceful bypass allowed.");
            return true; // Grant provisional approval during partial service failures
        } catch (Exception e) {
            return false;
        }
    }
}