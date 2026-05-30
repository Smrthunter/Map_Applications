package com.management_flow;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LayoutAnimationController;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();
    private TaskAdapter adapter;
    private String currentFilter = "All";
    private String searchQuery = "";
    
    private TextView tvSummary;
    private TextView tvEmptyState;
    private MaterialButton btnFilterAll, btnFilterActive, btnFilterCompleted;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSummary = findViewById(R.id.tv_summary);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilterActive = findViewById(R.id.btn_filter_active);
        btnFilterCompleted = findViewById(R.id.btn_filter_completed);
        searchView = findViewById(R.id.search_view);
        
        RecyclerView recyclerView = findViewById(R.id.recyclerTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this);
        recyclerView.setAdapter(adapter);

        // Item entry animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(animation);

        allTasks = TaskRepository.loadTasks(this);
        refreshList();

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddEditDialog(null));
        animateFabPulse(fab);

        setupFilters();
        setupSearch();
        setupSwipeToDelete(recyclerView);
    }

    private void animateFabPulse(View fab) {
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(fab,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.12f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.12f, 1.0f));
        pulse.setDuration(600);
        pulse.setRepeatCount(2);
        pulse.start();
    }

    private void setupFilters() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.btn_filter_all) currentFilter = "All";
            else if (id == R.id.btn_filter_active) currentFilter = "Active";
            else if (id == R.id.btn_filter_completed) currentFilter = "Completed";
            updateFilterButtons();
            refreshList();
        };
        btnFilterAll.setOnClickListener(listener);
        btnFilterActive.setOnClickListener(listener);
        btnFilterCompleted.setOnClickListener(listener);
    }

    private void updateFilterButtons() {
        int activeColor = ContextCompat.getColor(this, R.color.colorAccentViolet);
        int inactiveColor = ContextCompat.getColor(this, android.R.color.transparent);
        int activeText = ContextCompat.getColor(this, R.color.white);
        int inactiveText = ContextCompat.getColor(this, R.color.colorTextSecondary);

        btnFilterAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter.equals("All") ? activeColor : inactiveColor));
        btnFilterAll.setTextColor(currentFilter.equals("All") ? activeText : inactiveText);
        btnFilterActive.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter.equals("Active") ? activeColor : inactiveColor));
        btnFilterActive.setTextColor(currentFilter.equals("Active") ? activeText : inactiveText);
        btnFilterCompleted.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter.equals("Completed") ? activeColor : inactiveColor));
        btnFilterCompleted.setTextColor(currentFilter.equals("Completed") ? activeText : inactiveText);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.toLowerCase();
                refreshList();
                return true;
            }
        });
    }

    private void refreshList() {
        Collections.sort(allTasks, (t1, t2) -> {
            if (t1.isCompleted() != t2.isCompleted()) return t1.isCompleted() ? 1 : -1;
            return getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority());
        });

        filteredTasks.clear();
        int total = allTasks.size();
        int done = 0;
        for (Task t : allTasks) {
            if (t.isCompleted()) done++;
            
            boolean matchesFilter = currentFilter.equals("All") ||
                    (currentFilter.equals("Active") && !t.isCompleted()) ||
                    (currentFilter.equals("Completed") && t.isCompleted());
            
            boolean matchesSearch = t.getTitle().toLowerCase().contains(searchQuery);
            
            if (matchesFilter && matchesSearch) filteredTasks.add(t);
        }

        adapter.setTasks(filteredTasks, total, done, total - done, searchQuery);
        tvSummary.setText(getString(R.string.task_count_summary, total, total - done));
        tvEmptyState.setVisibility(filteredTasks.isEmpty() ? View.VISIBLE : View.GONE);
        TaskRepository.saveTasks(this, allTasks);
    }

    private int getPriorityValue(String p) {
        switch (p) {
            case "High": return 1;
            case "Medium": return 2;
            case "Low": return 3;
            default: return 4;
        }
    }

    private void showAddEditDialog(Task task) {
        AddEditTaskDialog.newInstance(task, updatedTask -> {
            if (task == null) allTasks.add(updatedTask);
            refreshList();
        }).show(getSupportFragmentManager(), "TaskDialog");
    }

    @Override
    public void onTaskChecked(int position, boolean isChecked) {
        Task task = filteredTasks.get(position);
        task.setCompleted(isChecked);
        refreshList();
    }

    @Override
    public void onTaskDeleted(int position) {
        Task task = filteredTasks.get(position);
        int originalIndex = allTasks.indexOf(task);
        allTasks.remove(task);
        refreshList();

        Snackbar.make(findViewById(R.id.main), R.string.task_deleted, 3000)
                .setAction(R.string.undo, v -> {
                    allTasks.add(originalIndex, task);
                    refreshList();
                })
                .setBackgroundTint(ContextCompat.getColor(this, R.color.colorSurface))
                .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccentCyan))
                .show();
    }

    @Override
    public void onTaskLongPressed(int position) {
        showAddEditDialog(filteredTasks.get(position));
    }

    @Override
    public void onTaskClicked(int position) {
        Task task = filteredTasks.get(position);
        TaskDetailFragment fragment = TaskDetailFragment.newInstance(task, new TaskDetailFragment.OnTaskDetailListener() {
            @Override
            public void onTaskUpdated() { refreshList(); }
            @Override
            public void onEditRequest(Task taskToEdit) { showAddEditDialog(taskToEdit); }
        });

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .add(R.id.main, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupSwipeToDelete(RecyclerView recyclerView) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (pos > 0) onTaskDeleted(pos - 1);
                else adapter.notifyItemChanged(0); // Can't delete header
            }
        }).attachToRecyclerView(recyclerView);
    }
}