package com.smartcampus.notification;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    private final NotificationStore store;

    public NotificationListener(NotificationStore store) {
        this.store = store;
    }

    @RabbitListener(queues = NotificationBrokerConfig.QUEUE_NAME)
    public void processNotificationMessage(String rawJson) {
        System.out.println("[MESSAGE CONSUMED] Event captured asynchronously: " + rawJson);
        store.add(rawJson); // Store the received event
    }
}