package com.isep.smarttripplanner.model;

import java.time.LocalDate;

public class TodoItem {
    private int id;
    private String tripId;
    private String description;
    private boolean isCompleted;
    private LocalDate dueDate;

    public TodoItem() {
    }

    public TodoItem(int id, String tripId, String description, boolean isCompleted, LocalDate dueDate) {
        this.id = id;
        this.tripId = tripId;
        this.description = description;
        this.isCompleted = isCompleted;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void toggleComplete() {
        this.isCompleted = !this.isCompleted;
    }
}
