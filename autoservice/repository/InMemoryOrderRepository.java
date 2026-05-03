package com.autoservice.repository;

import com.autoservice.domain.Order;
import com.autoservice.domain.OrderStatus;
import java.util.*;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> store = new LinkedHashMap<>();

    @Override
    public Order findById(String orderId) {
        return store.get(orderId);
    }

    @Override
    public void save(Order order) {
        store.put(order.getOrderId(), order);
    }

    @Override
    public void updateStatus(String orderId, OrderStatus status) {
        Order order = store.get(orderId);
        if (order != null) {
            order.setStatus(status);
        }
    }

    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Order> findByCustomerId(String customerId) {
        List<Order> result = new ArrayList<>();
        for (Order o : store.values()) {
            if (o.getCustomerId().equals(customerId)) result.add(o);
        }
        return result;
    }

    public List<Order> findByMechanicId(String mechanicId) {
        List<Order> result = new ArrayList<>();
        for (Order o : store.values()) {
            if (mechanicId.equals(o.getMechanicId())) result.add(o);
        }
        return result;
    }

    public List<Order> findByStatus(OrderStatus status) {
        List<Order> result = new ArrayList<>();
        for (Order o : store.values()) {
            if (o.getStatus() == status) result.add(o);
        }
        return result;
    }
}
