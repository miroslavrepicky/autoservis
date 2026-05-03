package com.autoservice.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceBook {
    private String serviceBookId;
    private int xnt;
    private String vehicleId;
    private String model;
    private List<String> entries;

    public ServiceBook(String vehicleId, String model) {
        this.serviceBookId = java.util.UUID.randomUUID().toString();
        this.vehicleId = vehicleId;
        this.model = model;
        this.entries = new ArrayList<>();
    }

    public void addEntry(String description, LocalDateTime date, String technicianId) {
        entries.add(date + " | " + technicianId + " | " + description);
    }

    public List<String> getEntries() { return entries; }

    public String getServiceBookId() { return serviceBookId; }
    public int getXnt() { return xnt; }
    public void setXnt(int xnt) { this.xnt = xnt; }
    public String getVehicleId() { return vehicleId; }
    public String getModel() { return model; }
}
