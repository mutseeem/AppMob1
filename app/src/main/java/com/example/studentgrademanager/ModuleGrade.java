package com.example.studentgrademanager;

public class ModuleGrade {
    private String moduleName;
    private Double grade;

    public ModuleGrade(String moduleName, Double grade) {
        this.moduleName = moduleName;
        this.grade = grade;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Double getGrade() {
        return grade;
    }
}
