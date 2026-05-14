package com.autoservice.employee;

public class Dispatcher extends Employee {

    public Dispatcher(int employeeId, String firstName, String lastName, String email) {
        super(employeeId, firstName, lastName, email, "Dispečer");
    }

    public void assignOrder(String orderId, int mechanicId, String vehicleId) {
        System.out.println(getFullName() + " priradil zákazku " + orderId + " mechanikovi " + mechanicId);
    }

    public int getWorkload() {
        return 0;
    }
}
