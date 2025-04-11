package com.example.studentgrademanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private Button btnAddStudent, btnAddTeacher, btnViewStudents;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);
        dbHelper.fetchAndInsertModules();
        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnAddTeacher = findViewById(R.id.btnAddTeacher);
        btnViewStudents = findViewById(R.id.btnViewStudents);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            finish();
        });

        btnAddStudent.setOnClickListener(v -> showAddStudentDialog());
        btnAddTeacher.setOnClickListener(v -> showAddTeacherDialog());
        btnViewStudents.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ViewStudentsActivity.class);
            startActivity(intent);
        });
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Student");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_student, null);
        final EditText etUsername = view.findViewById(R.id.etUsername);
        final EditText etPassword = view.findViewById(R.id.etPassword);
        final EditText etFullName = view.findViewById(R.id.etFullName);
        final Spinner spinnerGroup = view.findViewById(R.id.spinnerGroup);

        // Set up group spinner
        List<String> groups = dbHelper.getAllGroups();
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, groups);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(groupAdapter);

        builder.setView(view);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String group = spinnerGroup.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(AdminActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.addStudent(username, password, fullName, group);
                if (success) {
                    Toast.makeText(AdminActivity.this, "Student created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Failed to create student", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddTeacherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Teacher");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_teacher, null);
        final EditText etUsername = view.findViewById(R.id.etUsername);
        final EditText etPassword = view.findViewById(R.id.etPassword);
        final EditText etFullName = view.findViewById(R.id.etFullName);
        final EditText etGroups = view.findViewById(R.id.etGroups);
        final LinearLayout modulesContainer = view.findViewById(R.id.modulesContainer);

        // Add checkboxes for all modules
        List<String> modules = dbHelper.getAllModules();
        for (String module : modules) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(module);
            modulesContainer.addView(checkBox);
        }

        builder.setView(view);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String groupsInput = etGroups.getText().toString().trim();

            // Get selected modules
            List<String> selectedModules = new ArrayList<>();
            for (int i = 0; i < modulesContainer.getChildCount(); i++) {
                View child = modulesContainer.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) child;
                    if (checkBox.isChecked()) {
                        selectedModules.add(checkBox.getText().toString());
                    }
                }
            }

            // Process groups
            List<String> groups = new ArrayList<>();
            if (!groupsInput.isEmpty()) {
                for (String group : groupsInput.split(",")) {
                    String trimmedGroup = group.trim();
                    if (!trimmedGroup.isEmpty() && dbHelper.getAllGroups().contains(trimmedGroup)) {
                        groups.add(trimmedGroup);
                    }
                }
            }

            // Validation
            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Username, password and full name are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (groups.isEmpty()) {
                Toast.makeText(this, "At least one valid group is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedModules.isEmpty()) {
                Toast.makeText(this, "Select at least one module", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create teacher
            boolean success = dbHelper.addTeacher(username, password, fullName, groups);
            if (success) {
                // Assign modules to teacher
                User teacher = dbHelper.getUserByUsername(username);
                if (teacher != null) {
                    for (String moduleName : selectedModules) {
                        // Get module ID (this is simplified - you might need proper module lookup)
                        String moduleId = moduleName.substring(0, Math.min(moduleName.length(), 7));
                        for (String group : groups) {
                            dbHelper.assignModuleToTeacher(teacher.getId(), moduleId, group);
                        }
                    }
                    Toast.makeText(this, "Teacher added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to create teacher", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create teacher", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}