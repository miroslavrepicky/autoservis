package com.autoservice.repository;

import com.autoservice.domain.Order;
import com.autoservice.domain.OrderStatus;

public interface OrderRepository {
    Order findById(String orderId);
    void save(Order order);
    void updateStatus(String orderId, OrderStatus status);
}
