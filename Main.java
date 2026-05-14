package com.autoservice.notification;

public class EmailNotification implements INotificationChannel {

    @Override
    public String formatMessage(String message) {
        return "AutoServis s.r.o.\n" +
 "================================\n" +
               message + "\n" +
 "================================\n" +
 "Toto je automatická správa. Neodpovedajte na tento email.\n";
    }

    @Override
    public void send(String recipient, String message) {
        String formatted = formatMessage(message);
        System.out.println("[EMAIL] -> " + recipient + ":\n" + formatted);
    }
}
