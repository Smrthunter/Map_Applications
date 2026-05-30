package com.management_flow;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailFragment extends Fragment {

    public interface OnTaskDetailListener {
        void onTaskUpdated();
        void onEditRequest(Task task);
    }

    private Task task;
    private OnTaskDetailListener listener;

    public TaskDetailFragment() {}

    public static TaskDetailFragment newInstance(Task task, OnTaskDetailListener listener) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.task = task;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_detail, container, false);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        TextView tvPriority = view.findViewById(R.id.tv_detail_priority);
        TextView tvDate = view.findViewById(R.id.tv_detail_date);
        MaterialButton btnToggle = view.findViewById(R.id.btn_toggle_status);
        MaterialButton btnEdit = view.findViewById(R.id.btn_edit_detail);

        updateUI(tvTitle, tvPriority, tvDate, btnToggle);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnToggle.setOnClickListener(v -> {
            task.setCompleted(!task.isCompleted());
            updateUI(tvTitle, tvPriority, tvDate, btnToggle);
            if (listener != null) listener.onTaskUpdated();
        });

        btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditRequest(task);
        });

        return view;
    }

    private void updateUI(TextView tvTitle, TextView tvPriority, TextView tvDate, MaterialButton btnToggle) {
        tvTitle.setText(task.getTitle());
        tvPriority.setText(task.getPriority().toUpperCase());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDate.setText(getString(R.string.created_at, sdf.format(new Date(task.getCreatedAt()))));

        int priorityColor;
        Context context = requireContext();
        switch (task.getPriority()) {
            case "High": priorityColor = ContextCompat.getColor(context, R.color.colorHighPriority); break;
            case "Medium": priorityColor = ContextCompat.getColor(context, R.color.colorMedPriority); break;
            default: priorityColor = ContextCompat.getColor(context, R.color.colorLowPriority); break;
        }
        tvPriority.setTextColor(priorityColor);
        tvPriority.getBackground().setTint(adjustAlpha(priorityColor, 0.2f));

        if (task.isCompleted()) {
            btnToggle.setText("Mark as Active");
            btnToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccentViolet)));
        } else {
            btnToggle.setText("Mark as Completed");
            btnToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccentCyan)));
        }
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, red, green, blue);
    }
}