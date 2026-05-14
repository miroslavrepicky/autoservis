package com.autoservice.service;

import com.autoservice.domain.Order;

import java.time.LocalDateTime;
import java.util.*;

public class ScheduleManager {
    private final Set<LocalDateTime> blockedSlots = new HashSet<>();
    private final List<Order> scheduledOrders = new ArrayList<>();

    public void blockSlot(String vehicleId, LocalDateTime time) {
        blockedSlots.add(time);
    }

    public List<LocalDateTime> getAvailableSlots(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDateTime current = from.withMinute(0).withSecond(0).withNano(0);
        while (current.isBefore(to)) {
            if (!blockedSlots.contains(current) && isWorkingHour(current)) {
                slots.add(current);
            }
            current = current.plusHours(1);
        }
        return slots;
    }

    public boolean showAvailableSlots(LocalDateTime from, LocalDateTime to) {
        return !getAvailableSlots(from, to).isEmpty();
    }

    public void updateEstimatedTime(String orderId, LocalDateTime newTime) {
        // update
    }

    public void addToSchedule(Order order) {
        scheduledOrders.add(order);
        if (order.getAppointmentTime() != null) {
            blockedSlots.add(order.getAppointmentTime());
        }
    }

    public List<Order> getScheduledOrders() {
        return Collections.unmodifiableList(scheduledOrders);
    }

    private boolean isWorkingHour(LocalDateTime dt) {
        int hour = dt.getHour();
        int dow = dt.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        return dow <= 5 && hour >= 8 && hour < 17;
    }

    public int getMechanicWorkload(String mechanicId) {
        int count = 0;
        for (Order o : scheduledOrders) {
            if (mechanicId.equals(o.getMechanicId())) count++;
        }
        return count;
    }
}
