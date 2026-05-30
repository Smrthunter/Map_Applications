package com.management_flow;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

public class AddEditTaskDialog extends DialogFragment {

    public interface OnTaskSaveListener {
        void onTaskSave(Task task);
    }

    private OnTaskSaveListener listener;
    private Task existingTask;

    public AddEditTaskDialog() {}

    public static AddEditTaskDialog newInstance(Task task, OnTaskSaveListener listener) {
        AddEditTaskDialog dialog = new AddEditTaskDialog();
        dialog.existingTask = task;
        dialog.listener = listener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_task, container, false);
        
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        EditText etTitle = view.findViewById(R.id.et_task_title);
        Spinner spinnerPriority = view.findViewById(R.id.spinner_priority);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = view.findViewById(R.id.btn_save);

        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);

        if (existingTask != null) {
            tvTitle.setText(R.string.edit_task);
            etTitle.setText(existingTask.getTitle());
            btnSave.setText(R.string.save_task);
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equalsIgnoreCase(existingTask.getPriority())) {
                    spinnerPriority.setSelection(i);
                    break;
                }
            }
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), R.string.empty_title_error, Toast.LENGTH_SHORT).show();
                return;
            }

            String priority = (String) spinnerPriority.getSelectedItem();

            if (existingTask == null) {
                listener.onTaskSave(new Task(title, priority));
            } else {
                existingTask.setTitle(title);
                existingTask.setPriority(priority);
                listener.onTaskSave(existingTask);
            }
            dismiss();
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }
}