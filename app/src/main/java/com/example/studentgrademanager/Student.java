package com.example.studentgrademanager;

public class Student extends User {
    private String group;
    public Student(int id, String username, String fullName, Double grade) {
        super(id, username, fullName, grade);
        this.role = "student";
    }
    public void setGrade(Double grade) {
        this.grade = grade;
    }
}