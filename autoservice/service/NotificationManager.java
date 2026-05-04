package com.autoservice.service;

import com.autoservice.AppContext;
import com.autoservice.notification.EmailNotification;
import com.autoservice.notification.INotificationChannel;
import com.autoservice.notification.SMSNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NotificationManager {
    private final List<Consumer<String>> inAppListeners = new ArrayList<>();

    // Pending notifications per mechanicId – delivered when mechanic registers listener
    private final Map<String, List<String>> pendingMechanicNotifs = new ConcurrentHashMap<>();

    public void alertReceptionTechnician(String message) {
        fireInApp("[PRIJÍMACÍ TECHNIK] " + message);
    }

    public void composeMessage(String orderId, String state) {}

    public String formatMessage(String raw) {
        return raw.trim();
    }

    public String chooseChannel(String recipient) {
        if (recipient.contains("@")) return "email";
        return "sms";
    }

    public void notifyCustomer(String recipient, String message) {
        String channel = chooseChannel(recipient);
        INotificationChannel ch = channel.equals("email") ? new EmailNotification() : new SMSNotification();
        sendNotification(ch, recipient, message);
    }

    public void notifyEmployee(String employeeId, String message) {
        fireInApp("[PRE ZAMESTNANCA " + employeeId + "] " + message);
    }

    public void notifyMechanic(String mechanicId, String message) {
        String tagged = "[MECHANIK " + mechanicId + "] " + message;
        fireInApp(tagged);
        // Store as pending so the mechanic sees it even after logging in later
        pendingMechanicNotifs
                .computeIfAbsent(mechanicId, k -> new ArrayList<>())
                .add(tagged);
    }

    public void notifyStorekeeper(String storekeeperInfo, String message) {
        fireInApp("[SKLADNÍK] " + message);
        AppContext.getInstance().addStorekeeperNotif(message);
    }

    public boolean recordResult(String notificationId, boolean success) {
        System.out.println("Výsledok notifikácie " + notificationId + ": " + (success ? "OK" : "ZLYHALO"));
        return success;
    }

    public void retryDelivery(String notificationId) {
        fireInApp("[RETRY] Opakované odoslanie notifikácie: " + notificationId);
    }

    public void send(INotificationChannel channel, String recipient) {
        channel.send(recipient, "správa");
    }

    public void sendNotification(INotificationChannel channel, String recipient, String message) {
        try {
            channel.send(recipient, message);
            recordResult(recipient, true);
        } catch (Exception e) {
            recordResult(recipient, false);
            retryDelivery(recipient);
            alertReceptionTechnician("Doručenie zlyhalo pre: " + recipient);
        }
    }

    public void sendSmsNotification(String phone, String message) {
        sendNotification(new SMSNotification(), phone, message);
    }

    public void sendEmailNotification(String email, String message) {
        sendNotification(new EmailNotification(), email, message);
    }

    /**
     * Register a listener for in-app notifications.
     * mechanicId – when non-null, pending notifications for that mechanic are
     * immediately delivered to the new listener.
     */
    public void addInAppListener(Consumer<String> listener) {
        inAppListeners.add(listener);
    }

    /**
     * Register mechanic listener and flush any pending notifications for that mechanic.
     */
    public void addMechanicListener(String mechanicId, Consumer<String> listener) {
        inAppListeners.add(listener);
        List<String> pending = pendingMechanicNotifs.remove(mechanicId);
        if (pending != null) {
            for (String msg : pending) {
                listener.accept(msg);
            }
        }
    }

    private void fireInApp(String message) {
        for (Consumer<String> listener : inAppListeners) {
            listener.accept(message);
        }
        System.out.println("[IN-APP NOTIF] " + message);
    }
}