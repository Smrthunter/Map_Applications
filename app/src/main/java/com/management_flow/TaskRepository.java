package com.management_flow;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private static final String TAG = "TaskRepository";
    private static final String PREF_NAME = "taskflow_prefs";
    private static final String KEY_TASKS = "tasks_json";

    /**
     * Saves the list of tasks to SharedPreferences as a JSON array string.
     *
     * @param context the application context
     * @param tasks   the list of tasks to save
     */
    public static void saveTasks(Context context, List<Task> tasks) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        try {
            for (Task task : tasks) {
                jsonArray.put(task.toJson());
            }
            prefs.edit().putString(KEY_TASKS, jsonArray.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving tasks", e);
        }
    }

    /**
     * Loads the list of tasks from SharedPreferences.
     *
     * @param context the application context
     * @return the list of tasks loaded, or an empty list if none found or on error
     */
    public static List<Task> loadTasks(Context context) {
        List<Task> tasks = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_TASKS, null);
        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    tasks.add(Task.fromJson(jsonArray.getJSONObject(i)));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error loading tasks", e);
            }
        }
        return tasks;
    }
}