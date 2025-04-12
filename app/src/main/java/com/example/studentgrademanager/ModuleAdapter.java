package com.example.studentgrademanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {
    private List<ModuleGrade> moduleGrades;

    public ModuleAdapter(List<ModuleGrade> moduleGrades) {
        this.moduleGrades = moduleGrades;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleGrade moduleGrade = moduleGrades.get(position);
        holder.moduleName.setText(moduleGrade.getModuleName());

        if (moduleGrade.getGrade() != null) {
            holder.grade.setText(String.format(Locale.getDefault(), "Grade: %.1f", moduleGrade.getGrade()));
        } else {
            holder.grade.setText("Not graded yet");
        }
    }

    @Override
    public int getItemCount() {
        return moduleGrades.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView moduleName;
        public TextView grade;

        public ViewHolder(View itemView) {
            super(itemView);
            moduleName = itemView.findViewById(R.id.tvModuleName);
            grade = itemView.findViewById(R.id.tvGrade);
        }
    }
}