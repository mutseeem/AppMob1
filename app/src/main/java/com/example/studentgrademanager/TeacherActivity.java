package com.example.studentgrademanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private List<Student> studentList;
    private DatabaseHelper dbHelper;
    private String teacherUsername;
    private String teacherModuleCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        teacherUsername = getIntent().getStringExtra("teacherUsername");
        System.out.println("DEBUG: TeacherActivity - Logged in teacher: " + teacherUsername);

        dbHelper = new DatabaseHelper(this);
        teacherModuleCode = dbHelper.getTeacherModuleCode(teacherUsername);
        System.out.println("DEBUG: TeacherActivity - Teacher module code: " + teacherModuleCode);

        recyclerView = findViewById(R.id.recyclerViewTeacher);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentList = dbHelper.getStudentsForModule(teacherModuleCode);
        System.out.println("DEBUG: TeacherActivity - Number of students loaded: " + studentList.size());

        adapter = new TeacherAdapter(studentList, teacherModuleCode);
        recyclerView.setAdapter(adapter);
    }
}
