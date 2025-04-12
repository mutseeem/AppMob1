package com.example.studentgrademanager;

public class Student extends User {
    private Double grade;

    // Constructor without grade (for when grade isn't available)
    public Student(int id, String username, String fullName, String group) {
        super(id, username, fullName, "student", group);
        this.grade = null;
    }

    // Constructor with grade
    public Student(int id, String username, String fullName, String group, Double grade) {
        super(id, username, fullName, "student", group);
        this.grade = grade;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }
}