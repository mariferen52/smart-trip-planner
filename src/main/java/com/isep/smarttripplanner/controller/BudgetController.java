package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Budget;
import com.isep.smarttripplanner.model.Expense;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.repository.ExpenseRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.List;

public class BudgetController {
    @FXML
    private ProgressBar budgetProgressBar;
    @FXML
    private Label spentLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private TextField limitField;
    @FXML
    private TextField expenseDescField;
    @FXML
    private TextField expenseAmountField;
    @FXML
    private ComboBox<String> categoryComboBox;

    private Budget currentBudget;
    private Trip currentTrip;
    private final ExpenseRepository expenseRepository = new ExpenseRepository();

    public void initData(Trip trip) {
        this.currentTrip = trip;
        this.currentBudget = new Budget(trip.getBudget(), "USD"); // Default currency
        List<Expense> expenses = expenseRepository.findExpensesByTripId(trip.getId());
        for (Expense e : expenses) {
            currentBudget.addExpense(e);
        }
        updateUI();
    }

    @FXML
    private void handleUpdateLimit() {
        try {
            double limit = Double.parseDouble(limitField.getText());
            currentBudget.setTotalLimit(limit);
            // In a real app, you'd update the trip budget in TripRepository too
            updateUI();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the budget limit.");
        }
    }

    @FXML
    private void handleAddExpense() {
        try {
            String desc = expenseDescField.getText();
            double amount = Double.parseDouble(expenseAmountField.getText());
            String category = categoryComboBox.getValue();

            if (desc.isEmpty() || category == null) {
                showAlert("Missing Info", "Please fill in all expense details.");
                return;
            }

            Expense expense = new Expense(0, currentTrip.getId(), desc, amount, category, LocalDate.now());
            expenseRepository.insertExpense(expense);
            currentBudget.addExpense(expense);

            expenseDescField.clear();
            expenseAmountField.clear();
            updateUI();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the expense amount.");
        }
    }

    private void updateUI() {
        double spent = currentBudget.getCurrentSpent();
        double limit = currentBudget.getTotalLimit();
        double balance = currentBudget.getRemainingBalance();

        spentLabel.setText(String.format("Spent: $%.2f", spent));
        balanceLabel.setText(String.format("Balance: $%.2f", balance));

        double progress = limit > 0 ? spent / limit : 0;
        budgetProgressBar.setProgress(Math.min(progress, 1.0));

        if (progress > 0.9) {
            budgetProgressBar.setStyle("-fx-accent: red;");
        } else {
            budgetProgressBar.setStyle("-fx-accent: #4caf50;");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
