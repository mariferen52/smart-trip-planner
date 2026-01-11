package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Expense;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.repository.ExpenseRepository;
import com.isep.smarttripplanner.repository.TripRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.List;

public class TotalBudgetController {
    @FXML
    private Label totalBudgetLabel;
    @FXML
    private Label totalSpentLabel;
    @FXML
    private VBox tripSummaryContainer;

    private final TripRepository tripRepository = new TripRepository();
    private final ExpenseRepository expenseRepository = new ExpenseRepository();

    @FXML
    public void initialize() {
        loadGlobalSummary();
    }

    private void loadGlobalSummary() {
        List<Trip> trips = tripRepository.findAllTrips();
        double globalBudget = 0;
        double globalSpent = 0;

        tripSummaryContainer.getChildren().clear();

        for (Trip trip : trips) {
            globalBudget += trip.getBudget();
            List<Expense> expenses = expenseRepository.findExpensesByTripId(trip.getId());
            double tripSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
            globalSpent += tripSpent;

            Label tripLabel = new Label(String.format("%s: $%.2f / $%.2f",
                    trip.getTitle(), tripSpent, trip.getBudget()));
            tripSummaryContainer.getChildren().add(tripLabel);
        }

        totalBudgetLabel.setText(String.format("Total Global Budget: $%.2f", globalBudget));
        totalSpentLabel.setText(String.format("Total Global Spent: $%.2f", globalSpent));
    }
}
