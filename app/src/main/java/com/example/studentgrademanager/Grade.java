package com.example.studentgrademanager;

public class Grade {
    private String moduleName;
    private double gradeValue;

    public Grade(String moduleName, double gradeValue) {
        this.moduleName = moduleName;
        this.gradeValue = gradeValue;
    }

    // Getters
    public String getModuleName() {
        return moduleName;
    }

    public double getGradeValue() {
        return gradeValue;
    }

    // Setters
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setGradeValue(double gradeValue) {
        this.gradeValue = gradeValue;
    }
}