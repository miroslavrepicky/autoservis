package com.autoservice.service;

import com.autoservice.domain.*;

import java.util.*;

public class ProfileManager {
    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private final Map<String, TempProfile> tempProfiles = new LinkedHashMap<>();

    public void addProfileToOrder(String orderId, String customerId) {
        // link profile to order
    }

    public void addTempProfile(String firstName, String lastName, String phone, TempProfile profile) {
        tempProfiles.put(profile.getTempId(), profile);
    }

    public void addVehicleToProfile(String profileId, String vehicleId) {
        // link vehicle
    }

    public void saveProfile(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
    }

    public Customer findCustomerById(String customerId) {
        return customers.get(customerId);
    }

    public Customer findCustomerByEmail(String email) {
        return customers.values().stream()
            .filter(c -> email.equalsIgnoreCase(c.getEmail()))
            .findFirst().orElse(null);
    }

    public TempProfile createTempProfile(String firstName, String lastName, String phone, String email) {
        TempProfile tp = new TempProfile(firstName, lastName, phone, email);
        tempProfiles.put(tp.getTempId(), tp);
        return tp;
    }

    public Customer convertTempToFull(String tempId, String password) {
        TempProfile tp = tempProfiles.get(tempId);
        if (tp == null) return null;
        Customer customer = tp.convertToFull(tp.getFirstName(), tp.getLastName());
        customers.put(customer.getCustomerId(), customer);
        tempProfiles.remove(tempId);
        return customer;
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    public List<TempProfile> getAllTempProfiles() {
        return new ArrayList<>(tempProfiles.values());
    }

    public boolean isEmailRegistered(String email) {
        return customers.values().stream()
            .anyMatch(c -> email.equalsIgnoreCase(c.getEmail()));
    }

    public TempProfile getTempProfile(String tempId) {
        return tempProfiles.get(tempId);
    }
}
