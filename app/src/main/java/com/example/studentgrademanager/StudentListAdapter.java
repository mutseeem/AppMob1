package com.example.studentgrademanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.ViewHolder> {
    private List<User> students;

    public StudentListAdapter(List<User> students) {
        this.students = students;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvStudentName;
        public TextView tvStudentGroup;

        public ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentGroup = itemView.findViewById(R.id.tvStudentGroup);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User student = students.get(position);
        holder.tvStudentName.setText(student.getFullName());
        holder.tvStudentGroup.setText("Group: " + student.getGroup());
    }

    @Override
    public int getItemCount() {
        return students.size();
    }
}