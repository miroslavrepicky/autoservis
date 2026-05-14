package com.autoservice.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Požiadavka mechanika na náhradný diel.
 * Cena je neznáma – doplní ju skladník pri objednaní.
 */
public class PartRequest {

    public enum Status { PENDING, ORDERED, DELIVERED, CANCELLED }

    private final String requestId;
    private final String orderId;       // ku ktorej zákazke patrí
    private final String mechanicId;
    private String partName;            // názov dielu zadaný mechanikom
    private int quantity;
    private double finalPrice;          // 0 kým skladník nevyplní
    private Status status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PartRequest(String orderId, String mechanicId, String partName, int quantity) {
        this.requestId  = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.orderId    = orderId;
        this.mechanicId = mechanicId;
        this.partName   = partName;
        this.quantity   = quantity;
        this.finalPrice = 0.0;
        this.status     = Status.PENDING;
        this.createdAt  = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
    }

    public void markOrdered(double price) {
        this.finalPrice = price;
        this.status     = Status.ORDERED;
        this.updatedAt  = LocalDateTime.now();
    }

    public void markDelivered() {
        this.status    = Status.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status    = Status.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    // getters
    public String getRequestId()  { return requestId; }
    public String getOrderId()    { return orderId; }
    public String getMechanicId() { return mechanicId; }
    public String getPartName()   { return partName; }
    public int    getQuantity()   { return quantity; }
    public double getFinalPrice() { return finalPrice; }
    public Status getStatus()     { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}