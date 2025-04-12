package com.example.studentgrademanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {
    private List<Student> students;
    private String currentModuleId;
    private TeacherActivity teacherActivity;

    public TeacherAdapter(List<Student> students, String moduleId) {
        this.students = students;
        this.currentModuleId = moduleId;
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

        // Set current grade if exists
        holder.etGrade.setText(student.getGrade() == null ? "" : String.valueOf(student.getGrade()));

        holder.btnSubmit.setOnClickListener(v -> {
            try {
                double grade = Double.parseDouble(holder.etGrade.getText().toString());
                if (grade < 0 || grade > 100) {
                    holder.etGrade.setError("Grade must be between 0-100");
                    return;
                }

                if (teacherActivity == null && v.getContext() instanceof TeacherActivity) {
                    teacherActivity = (TeacherActivity) v.getContext();
                }

                if (teacherActivity != null) {
                    teacherActivity.submitGrade(student.getId(), currentModuleId, grade);
                    student.setGrade(grade); // Update the local student object
                }
            } catch (NumberFormatException e) {
                holder.etGrade.setError("Enter a valid number");
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }
}