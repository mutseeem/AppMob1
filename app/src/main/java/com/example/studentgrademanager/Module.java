package com.example.studentgrademanager;

public class Module {
    private String moduleId;
    private String moduleName;
    private String group;
    private Double grade;

    public Module() {}

    public Module(String moduleId, String moduleName, String group) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.group = group;
    }

    // Getters and setters
    public String getModuleId() { return moduleId; }
    public String getModuleName() { return moduleName; }
    public String getGroup() { return group; }
    public Double getGrade() { return grade; }

    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public void setGroup(String group) { this.group = group; }
    public void setGrade(Double grade) { this.grade = grade; }
}