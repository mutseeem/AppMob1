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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity {
    private Button btnAddStudent, btnAddTeacher;
    private DatabaseHelper dbHelper;
    private List<String> moduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        dbHelper = new DatabaseHelper(this);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnAddTeacher = findViewById(R.id.btnAddTeacher);

        // Fetch modules from API
        fetchModulesFromAPI();

        btnAddStudent.setOnClickListener(v -> showAddStudentDialog());
        btnAddTeacher.setOnClickListener(v -> showAddTeacherDialog());
    }

    private void fetchModulesFromAPI() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://num.univ-biskra.dz/psp/formations/get_modules_json?sem=1&spec=184")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AdminActivity.this, "Failed to fetch modules", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(jsonResponse);
                        moduleList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject module = jsonArray.getJSONObject(i);
                            String moduleName = module.getString("Nom_module");
                            String moduleCode = module.getString("id_module");
                            moduleList.add(moduleName + " (" + moduleCode + ")");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
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

        builder.setView(view);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String group = spinnerGroup.getSelectedItem().toString().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || group.isEmpty()) {
                Toast.makeText(AdminActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.addUser(username, password, "student", fullName, group);
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

        // Populate modules dynamically
        for (String module : moduleList) {
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

            List<String> groups = new ArrayList<>();
            if (!groupsInput.isEmpty()) {
                for (String group : groupsInput.split(",")) {
                    if (!group.trim().isEmpty()) {
                        groups.add(group.trim());
                    }
                }
            }

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (selectedModules.isEmpty()) {
                Toast.makeText(this, "Select at least one module", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.addTeacher(username, password, fullName, groups);
                if (success) {
                    Toast.makeText(this, "Teacher created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to create teacher", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

}
