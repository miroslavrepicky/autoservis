package com.autoservice.employee;

public class Storekeeper extends Employee {

    public Storekeeper(int employeeId, String firstName, String lastName, String email) {
        super(employeeId, firstName, lastName, email, "Skladník");
    }

    public void confirmFreeDelivery(String partId) {
        System.out.println(getFullName() + " potvrdil doručenie dielu " + partId);
    }

    public void orderParts(String partId, String orderId) {
        System.out.println(getFullName() + " objednáva diel " + partId + " pre zákazku " + orderId);
    }
}
