package com.autoservice.employee;

public abstract class Employee {
    protected String email;
    protected int employeeId;
    protected String firstName;
    protected String lastName;
    protected String role;

    public Employee(int employeeId, String firstName, String lastName, String email, String role) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public boolean login(String username, String password) {
        return username.equals(email) && !password.isEmpty();
    }

    public String getEmail() { return email; }
    public int getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return role + ": " + getFullName() + " [" + employeeId + "]";
    }
}
