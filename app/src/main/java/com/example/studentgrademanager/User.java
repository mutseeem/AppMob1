package com.example.studentgrademanager;

public class User {
    protected int id;
    protected String username;
    protected String password;
    protected String fullName;
    protected String role;
    protected String group;

    public User(int id, String username, String fullName, String role, String group) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.group = group;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getGroup() { return group; }

    // Setters
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setGroup(String group) { this.group = group; }
}