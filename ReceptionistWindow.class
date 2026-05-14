package com.autoservice.domain;

public class WorkItem {
    private String description;
    private double laborCost;
    private String workItemId;

    public WorkItem(String workItemId, String description, double laborCost) {
        this.workItemId = workItemId;
        this.description = description;
        this.laborCost = laborCost;
    }

    public void save(String workItemId, String description, double laborCost) {
        this.workItemId = workItemId;
        this.description = description;
        this.laborCost = laborCost;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getLaborCost() { return laborCost; }
    public void setLaborCost(double laborCost) { this.laborCost = laborCost; }
    public String getWorkItemId() { return workItemId; }
    public void setWorkItemId(String workItemId) { this.workItemId = workItemId; }

    @Override
    public String toString() {
        return description + " - " + laborCost + " EUR";
    }
}
