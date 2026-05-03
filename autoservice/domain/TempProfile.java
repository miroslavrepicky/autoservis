package com.autoservice.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TempProfile {
    private LocalDateTime createdAt;
    private String tempId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private List<Vehicle> vehicles;

    public TempProfile(String firstName, String lastName, String phone, String email) {
        this.tempId = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.vehicles = new ArrayList<>();
    }

    public Customer convertToFull(String firstName, String lastName) {
        Customer customer = new Customer(firstName, lastName, email, phone, "");
        customer.setTemporary(false);
        return customer;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getTempId() { return tempId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Vehicle> getVehicles() { return vehicles; }

    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return "TempProfile: " + getFullName() + " (" + phone + ")";
    }
}