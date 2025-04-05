package com.example.studentgrademanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {
    private List<Student> students;
    private String moduleCode;

    public TeacherAdapter(List<Student> students, String moduleCode) {
        this.students = students;
        this.moduleCode = moduleCode;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public TextView tvFullName;
        public EditText etGrade;
        public Button btnSubmit;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            etGrade = itemView.findViewById(R.id.etGrade);
            btnSubmit = itemView.findViewById(R.id.btnSubmitGrade);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Student student = students.get(position);
        holder.tvUsername.setText(student.getUsername());
        holder.tvFullName.setText(student.getFullName());
        holder.etGrade.setText(student.getGrade() == null ? "" : String.valueOf(student.getGrade()));

        holder.btnSubmit.setOnClickListener(v -> {
            String gradeStr = holder.etGrade.getText().toString().trim();
            if (!gradeStr.isEmpty()) {
                try {
                    double grade = Double.parseDouble(gradeStr);
                    if (grade < 0 || grade > 20) {
                        holder.etGrade.setError("Grade must be between 0 and 20");
                        return;
                    }
                    DatabaseHelper dbHelper = new DatabaseHelper(v.getContext());
                    boolean success = dbHelper.updateStudentGrade(student.getId(), grade, moduleCode);
                    if (success) {
                        Toast.makeText(v.getContext(), "Grade updated for " + student.getUsername(), Toast.LENGTH_SHORT).show();
                        student.setGrade(grade);
                    } else {
                        Toast.makeText(v.getContext(), "Failed to update grade", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(v.getContext(), "Enter a valid grade", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Grade cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }
}