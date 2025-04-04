package com.example.studentgrademanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private List<ModuleGrade> moduleGrades;

    public StudentAdapter(List<ModuleGrade> moduleGrades) {
        this.moduleGrades = moduleGrades;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvModuleName, tvGrade;

        public ViewHolder(View itemView) {
            super(itemView);
            tvModuleName = itemView.findViewById(R.id.tvModuleName);
            tvGrade = itemView.findViewById(R.id.tvGrade);
        }
    }

    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StudentAdapter.ViewHolder holder, int position) {
        ModuleGrade moduleGrade = moduleGrades.get(position);
        holder.tvModuleName.setText(moduleGrade.getModuleName());
        // Display grade if available; otherwise, show a placeholder
        holder.tvGrade.setText(moduleGrade.getGrade() == null ? "" : String.valueOf(moduleGrade.getGrade()));
    }

    @Override
    public int getItemCount() {
        return moduleGrades.size();
    }
}
