package com.management_flow;

import org.json.JSONException;
import org.json.JSONObject;

public class Task {
    private long id;
    private String title;
    private String priority; // "High" | "Medium" | "Low"
    private boolean isCompleted;
    private long createdAt;

    public Task(String title, String priority) {
        this.id = System.currentTimeMillis();
        this.title = title;
        this.priority = priority;
        this.isCompleted = false;
        this.createdAt = System.currentTimeMillis();
    }

    public Task(long id, String title, String priority, boolean isCompleted, long createdAt) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public long getCreatedAt() { return createdAt; }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("title", title);
        json.put("priority", priority);
        json.put("isCompleted", isCompleted);
        json.put("createdAt", createdAt);
        return json;
    }

    public static Task fromJson(JSONObject json) throws JSONException {
        return new Task(
                json.getLong("id"),
                json.getString("title"),
                json.getString("priority"),
                json.getBoolean("isCompleted"),
                json.getLong("createdAt")
        );
    }
}