package com.example.studentgrademanager;

import java.util.List;

public class Teacher extends User {
    private List<String> modules;
    private List<String> groups;

    public Teacher(int id, String username, String password, String fullName,
                   List<String> modules, List<String> groups) {
        super(id, username, fullName, null);
        this.password = password;
        this.role = "teacher";
        this.modules = modules;
        this.groups = groups;
    }

    public List<String> getModules() { return modules; }
    public List<String> getGroups() { return groups; }
}