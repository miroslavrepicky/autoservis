package com.autoservice.domain;

public class SparePart {
    private String name;
    private String partId;
    private String quantity;
    private double unitPrice;

    public SparePart(String partId, String name, double unitPrice, String quantity) {
        this.partId = partId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public double getTotalPrice(int qty) {
        return unitPrice * qty;
    }

    public void save(String partId, String name, double price) {
        this.partId = partId;
        this.name = name;
        this.unitPrice = price;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPartId() { return partId; }
    public void setPartId(String partId) { this.partId = partId; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    @Override
    public String toString() {
        return name + " [" + partId + "] - " + unitPrice + " EUR (qty: " + quantity + ")";
    }
}
