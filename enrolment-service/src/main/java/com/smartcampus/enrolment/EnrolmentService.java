package com.smartcampus.enrolment;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class EnrolmentService {

    private final EnrolmentRepository repository;
    private final StudentClient studentClient;
    private final RabbitTemplate rabbitTemplate;

    // R5: Custom Thread Pool managing system entry connections
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    // Shared mutable state: remaining capacity by course key
    private final ConcurrentHashMap<String, Integer> capacities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public EnrolmentService(EnrolmentRepository repository, StudentClient studentClient, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.studentClient = studentClient;
        this.rabbitTemplate = rabbitTemplate;
        
        // Setup initial mock course seats
        capacities.put("CS101", 3); // CS101 has a maximum limit of 3 seats
    }

    public CompletableFuture<EnrolmentResult> requestEnrolment(String studentId, String courseCode) {
        return CompletableFuture.supplyAsync(() -> {
            // Verify student exists using the resilient HTTP client (R4, R9)
            if (!studentClient.isStudentEligible(studentId)) {
                return new EnrolmentResult(false, "Verification failed: Student profile invalid.");
            }

            // Acquire lock dynamically per course to prevent race conditions (R5)
            ReentrantLock lock = locks.computeIfAbsent(courseCode, k -> new ReentrantLock());
            lock.lock();
            try {
                int currentCapacity = capacities.getOrDefault(courseCode, 0);
                if (currentCapacity <= 0) {
                    return new EnrolmentResult(false, "Registration failed: Capacity limit reached.");
                }

                // Simulate processing time
                Thread.sleep(150);

                // Update shared mutable state (R5)
                capacities.put(courseCode, currentCapacity - 1);

                // Save record to our database-per-service database (R3)
                Enrolment enrolment = repository.save(new Enrolment(studentId, courseCode, "ENROLLED"));

                // R6: Publish event asynchronously to RabbitMQ
                String messageJson = String.format(
                    "{\"eventId\":\"%s\",\"eventType\":\"COURSE_ENROLLED\",\"payload\":{\"studentId\":\"%s\",\"courseCode\":\"%s\"}}",
                    java.util.UUID.randomUUID(), studentId, courseCode
                );
                rabbitTemplate.convertAndSend("exchange.smartcampus", "student.enrolled", messageJson);

                return new EnrolmentResult(true, "Successfully registered for " + courseCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new EnrolmentResult(false, "Processing interrupted.");
            } finally {
                lock.unlock(); // Release lock safely
            }
        }, executorService);
    }
}