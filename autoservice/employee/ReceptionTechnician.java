package com.autoservice.employee;

public class ReceptionTechnician extends Employee {

    public ReceptionTechnician(int employeeId, String firstName, String lastName, String email) {
        super(employeeId, firstName, lastName, email, "Prijímací technik");
    }

    public void handoverVehicle() {
        System.out.println(getFullName() + " odovzdáva vozidlo zákazníkovi.");
    }

    public void receiveVehicle(String orderId, String vehicleId) {
        System.out.println(getFullName() + " prijíma vozidlo pre zákazku " + orderId);
    }
}
