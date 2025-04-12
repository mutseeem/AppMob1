package com.example.studentgrademanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private List<User> students;
    private String currentModuleId;
    private TeacherActivity teacherActivity;

    public StudentAdapter(List<User> students, String moduleId) {
        this.students = students;
        this.currentModuleId = moduleId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_grade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User student = students.get(position);
        holder.studentName.setText(student.getFullName());
        holder.studentUsername.setText(student.getUsername());

        holder.submitGrade.setOnClickListener(v -> {
            try {
                double grade = Double.parseDouble(holder.gradeInput.getText().toString());
                if (grade < 0 || grade > 100) {
                    holder.gradeInput.setError("Grade must be 0-100");
                    return;
                }

                if (teacherActivity == null && v.getContext() instanceof TeacherActivity) {
                    teacherActivity = (TeacherActivity) v.getContext();
                }

                if (teacherActivity != null) {
                    teacherActivity.submitGrade(student.getId(), currentModuleId, grade);
                }
            } catch (NumberFormatException e) {
                holder.gradeInput.setError("Enter valid number");
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView studentName;
        public TextView studentUsername;
        public EditText gradeInput;
        public Button submitGrade;

        public ViewHolder(View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.tvStudentName);
            studentUsername = itemView.findViewById(R.id.tvStudentUsername);
            gradeInput = itemView.findViewById(R.id.etGrade);
            submitGrade = itemView.findViewById(R.id.btnSubmitGrade);
        }
    }
}