package com.example.studentgrademanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<ModuleGrade> moduleGrades;
    private DatabaseHelper dbHelper;
    private String studentUsername;
    private TextView tvAverage; // TextView for average grade and pass/fail

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Get the student username from Intent extras
        studentUsername = getIntent().getStringExtra("studentUsername");
        System.out.println("DEBUG: StudentActivity - Logged in student: " + studentUsername);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewStudent);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvAverage = findViewById(R.id.tvAverage);

        moduleGrades = dbHelper.getModuleGradesForStudent(studentUsername);
        System.out.println("DEBUG: StudentActivity - Number of modules loaded: " + moduleGrades.size());

        adapter = new StudentAdapter(moduleGrades);
        recyclerView.setAdapter(adapter);

        // Check if all modules have a grade and calculate average if so.
        boolean allGraded = true;
        double sum = 0;
        for (ModuleGrade mg : moduleGrades) {
            if (mg.getGrade() == null) {
                allGraded = false;
                break;
            }
            sum += mg.getGrade();
        }
        if (allGraded && !moduleGrades.isEmpty()) {
            double average = sum / moduleGrades.size();
            String result = average >= 10 ? "Pass" : "Fail";
            tvAverage.setText("Average grade: " + String.format("%.2f", average) + " (" + result + ")");
        } else {
            tvAverage.setText("");
        }
    }
}
