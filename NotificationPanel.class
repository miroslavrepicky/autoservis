package com.autoservice.service;

import com.autoservice.domain.Customer;
import com.autoservice.domain.Vehicle;
import com.autoservice.domain.VehicleCard;

import java.util.*;

public class VehicleManager {
    private final Map<String, VehicleCard> vehicleCards = new LinkedHashMap<>();
    private final Map<String, Vehicle> vehiclesByVin = new HashMap<>();

    public void addToProfile(String customerId, String vehicleId) {
        // link existing vehicle to customer
    }

    public Vehicle createVehicleCard(String make, String model, String vin, String year,
                                    String engine, String licensePlate) {
        Vehicle vehicle = new Vehicle(vin, licensePlate, make, model, year, engine);
        vehiclesByVin.put(vin, vehicle);
        VehicleCard card = new VehicleCard(vehicle, null);
        vehicleCards.put(vehicle.getVehicleId(), card);
        return vehicle;
    }

    public double calculateCost(Vehicle vehicle) {
        return 0.0; // placeholder
    }

    public boolean findVehicle(String vehicleId) {
        return vehicleCards.containsKey(vehicleId);
    }

    public List<Vehicle> getVehicles(String customerId) {
        List<Vehicle> result = new ArrayList<>();
        for (VehicleCard card : vehicleCards.values()) {
            if (customerId.equals(card.getCustomerId())) {
                result.add(card.getVehicle());
            }
        }
        return result;
    }

    public boolean showAdditionalFaults(String vehicleId) {
        return false;
    }

    public boolean validateVIN(String vin) {
        if (vin == null || vin.length() != 17) return false;
        return vin.matches("[A-HJ-NPR-Z0-9]{17}");
    }

    public boolean validateLicensePlate(String plate) {
        if (plate == null || plate.isBlank()) return false;
        return plate.matches("[A-Z]{2}\\d{3}[A-Z]{2}") || plate.length() >= 5;
    }

    public VehicleCard getVehicleCard(String vehicleId) {
        return vehicleCards.get(vehicleId);
    }

    public boolean isVinRegistered(String vin) {
        return vehiclesByVin.containsKey(vin);
    }

    public VehicleCard createAndRegisterCard(Customer customer, Vehicle vehicle) {
        VehicleCard card = new VehicleCard(vehicle, customer.getCustomerId());
        vehicleCards.put(vehicle.getVehicleId(), card);
        vehiclesByVin.put(vehicle.getVin(), vehicle);
        customer.addVehicleCard(card);
        return card;
    }

    public List<VehicleCard> getAllVehicleCards() {
        return new ArrayList<>(vehicleCards.values());
    }

    public Vehicle findVehicleByVin(String vin) {
        return vehiclesByVin.get(vin);
    }
}
