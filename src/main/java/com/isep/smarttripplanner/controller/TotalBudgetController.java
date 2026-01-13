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
        try {
            com.isep.smarttripplanner.repository.AppConfigRepository configRepo = new com.isep.smarttripplanner.repository.AppConfigRepository();
            String displayCurrency = configRepo.getConfig().getDefaultCurrency();

            tripSummaryContainer.getChildren().clear();
            totalBudgetLabel.setText("Calculating...");
            totalSpentLabel.setText("Calculating...");

            List<Trip> trips = tripRepository.findAllTrips();

            processTripsAsync(trips, displayCurrency);

        } catch (Exception e) {
            totalBudgetLabel.setText("Error loading config");
        }
    }

    private void processTripsAsync(List<Trip> trips, String displayCurrency) {
        com.isep.smarttripplanner.service.ExchangeRateService exchangeService = new com.isep.smarttripplanner.service.ExchangeRateService();
        String symbol = getCurrencySymbol(displayCurrency);

        final double[] totals = new double[] { 0.0, 0.0 };
        final int[] processedCount = new int[] { 0 };
        int totalTrips = trips.size();

        if (totalTrips == 0) {
            updateLabels(0, 0, symbol);
            return;
        }

        for (Trip trip : trips) {
            String tripCurrency = trip.getCurrency() != null ? trip.getCurrency() : displayCurrency;

            if (tripCurrency.equals(displayCurrency)) {
                processTripAmount(trip, 1.0, symbol, totals, processedCount, totalTrips);
            } else {
                exchangeService.getExchangeRate(tripCurrency, displayCurrency).thenAccept(rate -> {
                    processTripAmount(trip, rate, symbol, totals, processedCount, totalTrips);
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        processedCount[0]++;
                        if (processedCount[0] == totalTrips) {
                            updateLabels(totals[0], totals[1], symbol);
                        }
                    });
                    return null;
                });
            }
        }
    }

    private void processTripAmount(Trip trip, double rate, String symbol, double[] totals, int[] processedCount,
            int totalTrips) {
        double rawBudget = trip.getBudget();
        List<Expense> expenses = expenseRepository.findExpensesByTripId(trip.getId());
        double rawSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();

        double convertedBudget = rawBudget * rate;
        double convertedSpent = rawSpent * rate;

        javafx.application.Platform.runLater(() -> {
            totals[0] += convertedBudget;
            totals[1] += convertedSpent;

            Label tripLabel = new Label(String.format("%s: Spent %s%.2f / Budget %s%.2f (Rate: %.2f)",
                    trip.getTitle(), symbol, convertedSpent, symbol, convertedBudget, rate));
            tripSummaryContainer.getChildren().add(tripLabel);

            processedCount[0]++;
            if (processedCount[0] == totalTrips) {
                updateLabels(totals[0], totals[1], symbol);
            }
        });
    }

    private void updateLabels(double totalBudget, double totalSpent, String symbol) {
        totalBudgetLabel.setText(String.format("Total Global Budget: %s%.2f", symbol, totalBudget));
        totalSpentLabel.setText(String.format("Total Global Spent: %s%.2f", symbol, totalSpent));
    }

    private String getCurrencySymbol(String code) {
        try {
            return java.util.Currency.getInstance(code).getSymbol();
        } catch (Exception e) {
            return code;
        }
    }
}
