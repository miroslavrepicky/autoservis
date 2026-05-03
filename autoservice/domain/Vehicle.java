package com.autoservice.domain;

public class Vehicle {
    private String engine;
    private String licensePlate;
    private String make;
    private String model;
    private String vehicleId;
    private String vin;
    private String year;

    public Vehicle(String vin, String licensePlate, String make, String model, String year, String engine) {
        this.vin = vin;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.year = year;
        this.engine = engine;
        this.vehicleId = java.util.UUID.randomUUID().toString();
    }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getVehicleId() { return vehicleId; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    @Override
    public String toString() {
        return make + " " + model + " (" + year + ") - " + licensePlate + " [VIN: " + vin + "]";
    }
}
