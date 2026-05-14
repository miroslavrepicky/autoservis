package com.autoservice.employee;

import com.autoservice.domain.Order;
import com.autoservice.domain.WorkItem;

import java.util.List;

public class Mechanic extends Employee {
    private int currentLoad;
    private String specialization;

    public Mechanic(int employeeId, String firstName, String lastName, String email, String specialization) {
        super(employeeId, firstName, lastName, email, "Mechanik");
        this.specialization = specialization;
        this.currentLoad = 0;
    }

    public void completeRepair(String orderId) {
        System.out.println(getFullName() + " dokončil opravu zákazky " + orderId);
        if (currentLoad > 0) currentLoad--;
    }

    public List<Order> getAssignedOrders(int mechanicId) {
        return List.of();
    }

    public int getWorkload(int mechanicId) {
        return currentLoad;
    }

    public void acceptOrder(String orderId) {
        currentLoad++;
        System.out.println(getFullName() + " prijal zákazku " + orderId);
    }

    public int getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(int currentLoad) { this.currentLoad = currentLoad; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
