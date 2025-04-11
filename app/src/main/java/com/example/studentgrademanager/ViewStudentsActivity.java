package com.example.studentgrademanager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViewStudentsActivity extends AppCompatActivity {
    private Spinner spinnerGroups;
    private RecyclerView recyclerViewStudents;
    private DatabaseHelper dbHelper;
    private StudentListAdapter studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_student);

        dbHelper = new DatabaseHelper(this);

        spinnerGroups = findViewById(R.id.spinnerGroups);
        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));

        // Set up group spinner
        setupGroupSpinner();
    }

    private void setupGroupSpinner() {
        List<String> groups = dbHelper.getAllGroups();
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, groups);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroups.setAdapter(groupAdapter);

        spinnerGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGroup = groups.get(position);
                loadStudentsForGroup(selectedGroup);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadStudentsForGroup(String group) {
        List<User> students = dbHelper.getStudentsByGroup(group);
        studentAdapter = new StudentListAdapter(students);
        recyclerViewStudents.setAdapter(studentAdapter);
    }
}