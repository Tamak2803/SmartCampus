package com.smartcampus.notification;

import org.springframework.stereotype.Component;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

@Component
public class NotificationStore {
    private final List<String> alerts = new CopyOnWriteArrayList<>();

    public void add(String alert) {
        alerts.add(0, alert); // Add to the top of the list
        if (alerts.size() > 5) {
            alerts.remove(5); // Cap storage to the last 5 events
        }
    }

    public List<String> getAlerts() {
        return alerts;
    }
}