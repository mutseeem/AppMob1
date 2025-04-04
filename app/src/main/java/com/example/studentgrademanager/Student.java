package com.example.studentgrademanager;

public class Student {
    private int id;
    private String username;
    private String fullName;
    private Double grade;

    public Student(int id, String username, String fullName, Double grade) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.grade = grade;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }
}
