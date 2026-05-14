package com.autoservice.domain;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String customerId;
    private String email;
    private String firstName;
    private String lastName;
    private String mechanicId;
    private String phone;
    private String password;
    private boolean isTemporary;
    private List<VehicleCard> vehicleCards;

    public Customer(String firstName, String lastName, String email, String phone, String password) {
        this.customerId = java.util.UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.isTemporary = false;
        this.vehicleCards = new ArrayList<>();
    }

    public static Customer register(String firstName, String lastName, String email, String phone, String password) {
        return new Customer(firstName, lastName, email, phone, password);
    }

    public boolean checkPassword(String password) {
        return this.password != null && this.password.equals(password);
    }

    public List<VehicleCard> getServiceHistory(String vehicleId) {
        List<VehicleCard> result = new ArrayList<>();
        for (VehicleCard card : vehicleCards) {
            if (card.getVehicle().getVehicleId().equals(vehicleId)) {
                result.add(card);
            }
        }
        return result;
    }

    public void addVehicleCard(VehicleCard card) {
        vehicleCards.add(card);
    }

    public boolean hasVehicle(String vin) {
        return vehicleCards.stream()
                .anyMatch(c -> c.getVehicle().getVin().equals(vin));
    }

    // Getters & Setters
    public String getCustomerId() { return customerId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getMechanicId() { return mechanicId; }
    public void setMechanicId(String mechanicId) { this.mechanicId = mechanicId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isTemporary() { return isTemporary; }
    public void setTemporary(boolean temporary) { isTemporary = temporary; }
    public List<VehicleCard> getVehicleCards() { return vehicleCards; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return getFullName() + " (" + email + ")" + (isTemporary ? " [DOČASNÝ]" : "");
    }
}