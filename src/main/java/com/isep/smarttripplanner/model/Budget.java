package com.isep.smarttripplanner.model;

import java.util.ArrayList;
import java.util.List;

public class Budget {
    private double totalLimit;
    private String currencyCode;
    private double currentSpent;
    private List<Expense> expenses;

    public Budget() {
        this.expenses = new ArrayList<>();
    }

    public Budget(double totalLimit, String currencyCode) {
        this.totalLimit = totalLimit;
        this.currencyCode = currencyCode;
        this.currentSpent = 0;
        this.expenses = new ArrayList<>();
    }

    public double getTotalLimit() {
        return totalLimit;
    }

    public void setTotalLimit(double totalLimit) {
        this.totalLimit = totalLimit;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public double getCurrentSpent() {
        return currentSpent;
    }

    public void setCurrentSpent(double currentSpent) {
        this.currentSpent = currentSpent;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public void addExpense(Expense e) {
        this.expenses.add(e);
        this.currentSpent += e.getAmount();
    }

    public double getRemainingBalance() {
        return totalLimit - currentSpent;
    }
}
