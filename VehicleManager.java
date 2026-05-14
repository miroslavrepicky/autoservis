package com.autoservice.notification;

public interface INotificationChannel {
    String formatMessage(String message);
    void send(String recipient, String message);
}
