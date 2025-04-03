package com.example.studentgrademanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
                    System.out.println("DEBUG: API Response = " + jsonResponse);
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
                        System.out.println("DEBUG: JSON Parsing Error - " + e.getMessage());
                    }
                } else {
                    System.out.println("DEBUG: Response Failed - Code: " + response.code());
                }
            }
        });
    }



    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Student");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        final EditText etUsername = view.findViewById(R.id.etUsername);
        final EditText etPassword = view.findViewById(R.id.etPassword);
        final EditText etFullName = view.findViewById(R.id.etFullName);

        builder.setView(view);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(AdminActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.addUser(username, password, "student", fullName);
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
        if (moduleList.isEmpty()) {
            Toast.makeText(this, "Modules still loading... Please wait", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Teacher");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_teacher, null);
        final EditText etUsername = view.findViewById(R.id.etUsername);
        final EditText etPassword = view.findViewById(R.id.etPassword);
        final EditText etFullName = view.findViewById(R.id.etFullName);
        final Spinner spinnerModules = view.findViewById(R.id.spinnerModules);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, moduleList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModules.setAdapter(adapter);

        builder.setView(view);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String selectedModule = spinnerModules.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(AdminActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.addUser(username, password, "teacher", fullName);
                if (success) {
                    String moduleCode = selectedModule.substring(selectedModule.indexOf("(") + 1, selectedModule.indexOf(")"));
                    dbHelper.assignModuleToTeacher(username, moduleCode);
                    Toast.makeText(AdminActivity.this, "Teacher created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Failed to create teacher", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

}