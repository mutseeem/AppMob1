package com.example.studentgrademanager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {
    private Spinner moduleSpinner;
    private RecyclerView studentsRecyclerView;
    private StudentAdapter studentAdapter;
    private DatabaseHelper dbHelper;
    private int currentTeacherId;
    private List<Module> teacherModules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        // Initialize views
        moduleSpinner = findViewById(R.id.moduleSpinner);
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get teacher ID from login
        currentTeacherId = getIntent().getIntExtra("teacherId", -1);
        if (currentTeacherId == -1) {
            Toast.makeText(this, "Teacher not recognized", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        // Load teacher's modules
        loadTeacherModules();

        // Set up module spinner selection
        moduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Module selectedModule = teacherModules.get(position);
                loadStudentsForModule(selectedModule);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadTeacherModules() {
        teacherModules = dbHelper.getTeacherModules(currentTeacherId);
        if (teacherModules.isEmpty()) {
            Toast.makeText(this, "No modules assigned to you", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> moduleDisplayNames = new ArrayList<>();
        for (Module module : teacherModules) {
            moduleDisplayNames.add(module.getModuleName() + " - Group " + module.getGroup());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                moduleDisplayNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moduleSpinner.setAdapter(adapter);
    }

    private void loadStudentsForModule(Module module) {
        List<User> students = dbHelper.getStudentsByGroup(module.getGroup());
        if (students.isEmpty()) {
            Toast.makeText(this, "No students in this group", Toast.LENGTH_SHORT).show();
        }

        studentAdapter = new StudentAdapter(students, module.getModuleId());
        studentsRecyclerView.setAdapter(studentAdapter);
    }
    public void submitGrade(int studentId, String moduleId, double grade) {
        boolean success = dbHelper.addOrUpdateGrade(studentId, moduleId, grade);
        if (success) {
            Toast.makeText(this, "Grade submitted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to submit grade", Toast.LENGTH_SHORT).show();
        }
    }
}