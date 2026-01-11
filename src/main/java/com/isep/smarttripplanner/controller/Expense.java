package com.isep.smarttripplanner.model;

import java.time.LocalDate;

public class Expense {
    private int id;
    private String tripId;
    private String description;
    private double amount;
    private String category;
    private LocalDate date;

    public Expense() {
    }

    public Expense(int id, String tripId, String description, double amount, String category, LocalDate date) {
        this.id = id;
        this.tripId = tripId;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
