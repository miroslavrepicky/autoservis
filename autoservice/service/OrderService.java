package com.autoservice.service;

import com.autoservice.domain.*;
import com.autoservice.repository.InMemoryOrderRepository;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    private final InMemoryOrderRepository orderRepository;
    private final NotificationManager notificationManager;
    private final ScheduleManager scheduleManager;
    private final Inventory inventory;

    public OrderService(InMemoryOrderRepository orderRepository,
                        NotificationManager notificationManager,
                        ScheduleManager scheduleManager,
                        Inventory inventory) {
        this.orderRepository = orderRepository;
        this.notificationManager = notificationManager;
        this.scheduleManager = scheduleManager;
        this.inventory = inventory;
    }

    public Order assignOrder(String orderId, int mechanicId, String vehicleId) {
        Order order = orderRepository.findById(orderId);
        if (order != null) {
            order.setMechanicId(String.valueOf(mechanicId));
            order.setStatus(OrderStatus.PRIRADENA);
            orderRepository.save(order);
        }
        return order;
    }

    public void calculateCost(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order != null) {
            order.calculateCost(order.getWorkItems(), order.getSpareParts());
            orderRepository.save(order);
        }
    }

    public void completeRepair(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order != null) {
            order.setStatus(OrderStatus.UZAVRETA);
            order.setClosedAt(LocalDateTime.now());
            orderRepository.save(order);
            notificationManager.notifyCustomer(
                order.getCustomerId(),
                "Vaše vozidlo je pripravené na vyzdvihnutie. Zákazka: " + orderId
            );
        }
    }

    public boolean findById(String orderId) {
        return orderRepository.findById(orderId) != null;
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }

    public Order getOrderDetail(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrders(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getVehicles(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public int getWorkloadOf(int mechanicId) {
        return orderRepository.findByMechanicId(String.valueOf(mechanicId)).size();
    }

    public void recordRepair(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order != null) {
            orderRepository.save(order);
        }
    }

    public Order saveOrder(Order order) {
        orderRepository.save(order);
        scheduleManager.addToSchedule(order);
        return order;
    }

    public void setWorkload(String workloadId) { }

    public List<Order> showAdditionalFaults(String orderId) {
        return orderRepository.findAll();
    }

    public List<Order> showAssignedOrders(int mechanicId) {
        return orderRepository.findByMechanicId(String.valueOf(mechanicId));
    }

    public Order showOrderDetails(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> showWorkload(int workloadId) {
        return orderRepository.findAll();
    }

    public void updateScope(String orderId, List<WorkItem> workItems) {
        Order order = orderRepository.findById(orderId);
        if (order != null) {
            for (WorkItem wi : workItems) order.addWorkItem(wi);
            orderRepository.save(order);
        }
    }

    public void setStatus(String orderId, OrderStatus status) {
        orderRepository.updateStatus(orderId, status);
        Order order = orderRepository.findById(orderId);
        if (order != null) {
            String msg = "Zákazka " + orderId.substring(0, 8) + " zmenila stav na: " + status.getDisplayName();
            if (order.getMechanicId() != null) {
                notificationManager.notifyMechanic(order.getMechanicId(), msg);
            }
        }
    }

    // UC02 - Reserve online appointment
    public Order createReservation(String customerId, String vehicleId,
                                   String description, LocalDateTime appointmentTime, String notes) {
        Order order = new Order(customerId, vehicleId, description, appointmentTime);
        order.setNotes(notes);
        order.setStatus(OrderStatus.REZERVOVANA);
        orderRepository.save(order);
        scheduleManager.addToSchedule(order);
        notificationManager.notifyEmployee("receptionist",
            "Nová rezervácia vytvorená zákazníkom " + customerId + " na " + appointmentTime);
        return order;
    }

    // UC07 - Approve repair extension
    public void approveRepairExtension(String orderId, List<WorkItem> approvedItems) {
        Order order = orderRepository.findById(orderId);
        if (order == null) return;
        for (WorkItem wi : approvedItems) order.addWorkItem(wi);

        boolean partsAvailable = true;
        for (SparePart sp : order.getSpareParts()) {
            int qty = 1;
            try { qty = Integer.parseInt(sp.getQuantity()); } catch (Exception ignored) {}
            if (!inventory.isAvailable(sp.getPartId(), qty)) {
                partsAvailable = false;
                break;
            }
        }

        if (partsAvailable) {
            order.setStatus(OrderStatus.OPRAVA);
            if (order.getMechanicId() != null) {
                notificationManager.notifyMechanic(order.getMechanicId(),
                    "Zákazník schválil rozšírenie opravy #" + orderId.substring(0, 8));
            }
        } else {
            for (SparePart sp : order.getSpareParts()) {
                inventory.markForOrder(sp.getPartId(), 1);
            }
            notificationManager.notifyStorekeeper("storekeeper",
                "Objednajte diely pre zákazku #" + orderId.substring(0, 8));
            order.setStatus(OrderStatus.CAKA_NA_DIELY);
            if (order.getMechanicId() != null) {
                notificationManager.notifyMechanic(order.getMechanicId(),
                    "Zákazka #" + orderId.substring(0, 8) + " čaká na diely");
            }
        }
        orderRepository.save(order);
    }

    public void rejectRepairExtension(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) return;
        order.setStatus(OrderStatus.CIASTOCNE_DOKONCENA);
        orderRepository.save(order);
        if (order.getMechanicId() != null) {
            notificationManager.notifyMechanic(order.getMechanicId(),
                "Zákazník zamietol rozšírenie zákazky #" + orderId.substring(0, 8));
        }
    }

    // UC05 - Receive vehicle
    public void receiveVehicle(String orderId, List<String> photoPaths) {
        Order order = orderRepository.findById(orderId);
        if (order == null) return;
        order.setStatus(OrderStatus.DIAGNOSTIKA);
        orderRepository.save(order);
        notificationManager.notifyEmployee("receptionist",
            "Vozidlo prijaté do servisu, zákazka: #" + orderId.substring(0, 8));
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersForMechanic(String mechanicId) {
        return orderRepository.findByMechanicId(mechanicId);
    }
}
