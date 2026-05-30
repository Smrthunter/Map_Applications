package com.management_flow;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_STATS = 0;
    private static final int VIEW_TYPE_TASK = 1;

    public interface OnTaskActionListener {
        void onTaskChecked(int position, boolean isChecked);
        void onTaskDeleted(int position);
        void onTaskLongPressed(int position);
        void onTaskClicked(int position);
    }

    private List<Task> tasks = new ArrayList<>();
    private final OnTaskActionListener listener;
    private int totalCount, doneCount, remainingCount;
    private String searchQuery = "";

    public TaskAdapter(OnTaskActionListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> newTasks, int total, int done, int remaining, String search) {
        this.totalCount = total;
        this.doneCount = done;
        this.remainingCount = remaining;
        this.searchQuery = search.toLowerCase();
        
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return tasks.size() > 0 ? tasks.size() + 1 : 0; }
            @Override
            public int getNewListSize() { return newTasks.size() > 0 ? newTasks.size() + 1 : 0; }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                if (oldPos == 0 && newPos == 0) return true;
                if (oldPos == 0 || newPos == 0) return false;
                return tasks.get(oldPos - 1).getId() == newTasks.get(newPos - 1).getId();
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                if (oldPos == 0 && newPos == 0) return true;
                if (oldPos == 0 || newPos == 0) return false;
                Task oldTask = tasks.get(oldPos - 1);
                Task newTask = newTasks.get(newPos - 1);
                return oldTask.getTitle().equals(newTask.getTitle()) &&
                        oldTask.getPriority().equals(newTask.getPriority()) &&
                        oldTask.isCompleted() == newTask.isCompleted();
            }
        });
        this.tasks = new ArrayList<>(newTasks);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_STATS : VIEW_TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_STATS) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stats_header, parent, false);
            return new StatsViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof StatsViewHolder) {
            ((StatsViewHolder) holder).bind(totalCount, doneCount, remainingCount);
        } else if (holder instanceof TaskViewHolder) {
            ((TaskViewHolder) holder).bind(tasks.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.isEmpty() ? 0 : tasks.size() + 1;
    }

    class StatsViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        TextView tvTotal, tvDone, tvRemaining;

        public StatsViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_circular);
            tvTotal = itemView.findViewById(R.id.tv_stat_total);
            tvDone = itemView.findViewById(R.id.tv_stat_done);
            tvRemaining = itemView.findViewById(R.id.tv_stat_remaining);
        }

        public void bind(int total, int done, int remaining) {
            tvTotal.setText(String.valueOf(total));
            tvDone.setText(String.valueOf(done));
            tvRemaining.setText(String.valueOf(remaining));
            int progress = total > 0 ? (done * 100) / total : 0;
            progressBar.setProgress(progress);
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View priorityDot;
        private final TextView tvTitle;
        private final TextView tvBadge;
        private final MaterialCheckBox checkBox;
        private final ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityDot = itemView.findViewById(R.id.view_priority_dot);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvBadge = itemView.findViewById(R.id.tv_priority_badge);
            checkBox = itemView.findViewById(R.id.checkbox_task);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    animateCheckbox(buttonView);
                    listener.onTaskChecked(getAdapterPosition() - 1, isChecked);
                }
            });

            btnDelete.setOnClickListener(v -> listener.onTaskDeleted(getAdapterPosition() - 1));

            itemView.setOnLongClickListener(v -> {
                listener.onTaskLongPressed(getAdapterPosition() - 1);
                return true;
            });

            itemView.setOnClickListener(v -> listener.onTaskClicked(getAdapterPosition() - 1));
        }

        private void animateCheckbox(View view) {
            ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(view,
                    PropertyValuesHolder.ofFloat("scaleX", 0.8f, 1.15f, 1.0f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1.15f, 1.0f));
            scale.setDuration(200);
            scale.start();
        }

        public void bind(Task task) {
            String title = task.getTitle();
            if (!searchQuery.isEmpty() && title.toLowerCase().contains(searchQuery)) {
                android.text.SpannableString spannable = new android.text.SpannableString(title);
                int start = title.toLowerCase().indexOf(searchQuery);
                int end = start + searchQuery.length();
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                        ContextCompat.getColor(itemView.getContext(), R.color.colorAccentCyan)),
                        start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvTitle.setText(spannable);
            } else {
                tvTitle.setText(title);
            }

            checkBox.setChecked(task.isCompleted());
            tvBadge.setText(task.getPriority().toUpperCase());

            Context context = itemView.getContext();
            int priorityColor;
            switch (task.getPriority()) {
                case "High": priorityColor = ContextCompat.getColor(context, R.color.colorHighPriority); break;
                case "Medium": priorityColor = ContextCompat.getColor(context, R.color.colorMedPriority); break;
                default: priorityColor = ContextCompat.getColor(context, R.color.colorLowPriority); break;
            }

            priorityDot.getBackground().setTint(priorityColor);
            tvBadge.setTextColor(priorityColor);
            tvBadge.getBackground().setTint(adjustAlpha(priorityColor, 0.2f));

            if (task.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.colorCompleted));
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccentCyan)));
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary));
                checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccentViolet)));
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
}