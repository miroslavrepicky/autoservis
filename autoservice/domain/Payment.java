package com.autoservice.domain;

import java.time.LocalDateTime;

public class Payment {
    private double amount;
    private String method;
    private String orderId;
    private boolean paid;
    private LocalDateTime paymentDate;
    private String paymentId;

    public Payment(String orderId, double amount, String method) {
        this.paymentId = java.util.UUID.randomUUID().toString();
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.paid = false;
    }

    public double getTotalPrice(int qty) {
        return amount * qty;
    }

    public void process(double amount, double tax, String method) {
        this.amount = amount;
        this.method = method;
        this.paid = true;
        this.paymentDate = LocalDateTime.now();
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getOrderId() { return orderId; }
    public boolean isPaid() { return paid; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public String getPaymentId() { return paymentId; }

    @Override
    public String toString() {
        return "Platba " + amount + " EUR (" + method + ")" + (paid ? " - ZAPLATENÉ" : " - NEZAPLATENÉ");
    }
}
