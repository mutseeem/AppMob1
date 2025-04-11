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
    private List<Module> modules;

    public ModuleAdapter(List<Module> modules) {
        this.modules = modules;
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
        Module module = modules.get(position);
        holder.moduleName.setText(module.getModuleName());

        if (module.getGrade() != null) {
            holder.grade.setText(String.format(Locale.getDefault(), "Grade: %.1f", module.getGrade()));
        } else {
            holder.grade.setText("Not graded yet");
        }
    }

    @Override
    public int getItemCount() {
        return modules.size();
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