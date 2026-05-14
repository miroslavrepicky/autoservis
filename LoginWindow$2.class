package com.autoservice.domain;

import java.util.ArrayList;
import java.util.List;

public class VehicleCard {
    private String cardId;
    private Vehicle vehicle;
    private String customerId;
    private List<String> serviceHistory;

    public VehicleCard(Vehicle vehicle, String customerId) {
        this.cardId = java.util.UUID.randomUUID().toString();
        this.vehicle = vehicle;
        this.customerId = customerId;
        this.serviceHistory = new ArrayList<>();
    }

    public String getCardId() { return cardId; }
    public Vehicle getVehicle() { return vehicle; }
    public String getCustomerId() { return customerId; }
    public List<String> getServiceHistory() { return serviceHistory; }

    public void addServiceRecord(String record) {
        serviceHistory.add(record);
    }

    @Override
    public String toString() {
        return "VehicleCard{" + vehicle + ", customer=" + customerId + "}";
    }
}
