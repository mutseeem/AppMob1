package com.example.studentgrademanager;

import java.util.List;

public class User {
    protected int id;
    protected String username;
    protected String password;
    protected String fullName;
    protected String role;
    protected String group;  // For students (single group)
    protected String groups; // For teachers (comma-separated groups)

    // Constructor for students
    public User(int id, String username, String fullName, String role, String group) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.group = group;
    }

    // Constructor for teachers
    public User(int id, String username, String fullName, String role, List<String> groups) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        setGroupsList(groups);
    }

    // Getters and setters
    public String getGroup() { return group; }
    public String getGroups() { return groups; }

    public void setGroup(String group) { this.group = group; }
    public void setGroupsList(List<String> groups) {
        this.groups = TextUtils.join(",", groups);
    }
    public List<String> getGroupsList() {
        return Arrays.asList(groups.split(","));
    }
}