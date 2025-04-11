package com.example.studentgrademanager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ModuleAdapter adapter;
    private List<Module> modules;
    private DatabaseHelper dbHelper;
    private User currentStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get current student (in a real app, you'd get this from login)
        currentStudent = new User(1, "student1", "John Doe", "student", "GroupA");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rvGrades);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load modules for student's group
        loadModules();
    }

    private void loadModules() {
        modules = dbHelper.getModulesByGroupForStudent(currentStudent.getGroup(), currentStudent.getId());
        adapter = new ModuleAdapter(modules);
        recyclerView.setAdapter(adapter);
    }
}