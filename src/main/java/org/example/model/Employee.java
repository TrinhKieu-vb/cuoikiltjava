package org.example.model;

public class Employee {
    private int id;
    private String fullName;

    public Employee(int id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}