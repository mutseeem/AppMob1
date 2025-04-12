package com.example.studentgrademanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        //dbHelper.resetdb();
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter username & password", Toast.LENGTH_SHORT).show();
                } else {
                    if (dbHelper.checkUser(username, password)) {
                        String role = dbHelper.getUserRole(username);
                        Intent intent;
                        if ("teacher".equals(role)) {
                            intent = new Intent(LoginActivity.this, TeacherActivity.class);
                            intent.putExtra("teacherUsername", username);
                        } else if ("admin".equals(role)) {
                            intent = new Intent(LoginActivity.this, AdminActivity.class);
                        } else if ("student".equals(role)) {
                            intent = new Intent(LoginActivity.this, StudentActivity.class);
                            intent.putExtra("studentUsername", username);
                        } else {
                            Toast.makeText(LoginActivity.this, "Unknown role!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid login!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}