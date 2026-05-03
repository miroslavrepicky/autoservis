package com.autoservice.notification;

public class SMSNotification implements INotificationChannel {
    private static final int MAX_SMS_LENGTH = 160;

    @Override
    public String formatMessage(String message) {
        String formatted = message.replaceAll("[^\\x00-\\x7F]", "?");
        if (formatted.length() > MAX_SMS_LENGTH) {
            formatted = formatted.substring(0, MAX_SMS_LENGTH - 3) + "...";
        }
        return formatted;
    }

    @Override
    public void send(String recipient, String message) {
        String formatted = formatMessage(message);
        System.out.println("[SMS] -> " + recipient + ": " + formatted);
    }
}
