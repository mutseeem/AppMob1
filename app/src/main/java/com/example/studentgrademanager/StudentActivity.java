package com.example.studentgrademanager;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentActivity extends AppCompatActivity {
    private RecyclerView gradesRecyclerView;
    private ModuleAdapter moduleAdapter;
    private DatabaseHelper dbHelper;
    private String studentUsername;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        dbHelper = new DatabaseHelper(this);
        studentUsername = getIntent().getStringExtra("studentUsername");
        studentId = dbHelper.getUserByUsername(studentUsername).getId();

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + studentUsername + "\nYour Grades");

        gradesRecyclerView = findViewById(R.id.rvGrades);
        gradesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadStudentGrades();
    }

    private void loadStudentGrades() {
        List<ModuleGrade> grades = dbHelper.getStudentGrades(studentId);
        moduleAdapter = new ModuleAdapter(grades);
        gradesRecyclerView.setAdapter(moduleAdapter);
    }
}