package com.example.studentgrademanager;

public class User {
    protected int id;
    protected String username;
    protected String password;
    protected String fullName;
    protected String role;
    protected Double grade;

    public User(int id, String username, String fullName, Double grade) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.grade = grade;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }
}