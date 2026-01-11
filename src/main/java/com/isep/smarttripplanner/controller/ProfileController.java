package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.AppConfig;
import com.isep.smarttripplanner.model.Expense;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.repository.AppConfigRepository;
import com.isep.smarttripplanner.repository.ExpenseRepository;
import com.isep.smarttripplanner.repository.TripRepository;
import com.isep.smarttripplanner.service.ExchangeRateService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileController {

    @FXML
    private Label totalTripsLabel;
    @FXML
    private Label completedTripsLabel;
    @FXML
    private Label plannedTripsLabel;
    @FXML
    private Label totalBudgetLabel;
    @FXML
    private Label destinationsLabel;

    private final TripRepository tripRepo = new TripRepository();
    private final ExpenseRepository expenseRepo = new ExpenseRepository();
    private final AppConfigRepository configRepo = new AppConfigRepository();
    private final ExchangeRateService exchangeService = new ExchangeRateService();

    @FXML
    public void initialize() {
        loadUserProfileAsync();
    }

    private void loadUserProfileAsync() {
        if (totalBudgetLabel != null)
            totalBudgetLabel.setText("Loading...");

        CompletableFuture.runAsync(() -> {
            try {
                int totalTrips = tripRepo.countTotalTrips();
                int completedTrips = repoCountCompletedTrips();
                int totalDestinations = tripRepo.countTotalDestinations();

                String activeTripStatus = "None";
                Trip activeTrip = tripRepo.findActiveTrip();
                if (activeTrip != null) {
                    if (activeTrip.getStartDate().isAfter(java.time.LocalDate.now())) {
                        activeTripStatus = "Planned";
                    } else {
                        activeTripStatus = "Ongoing";
                    }
                }

                AppConfig config = configRepo.getConfig();
                String displayCurrency = config.getDefaultCurrency();
                String symbol = getCurrencySymbol(displayCurrency);

                List<Trip> allTrips = tripRepo.findAllTrips();
                double totalGlobalSpent = 0.0;

                for (Trip trip : allTrips) {
                    List<Expense> expenses = expenseRepo.findExpensesByTripId(trip.getId());
                    double tripSpentRaw = expenses.stream().mapToDouble(Expense::getAmount).sum();

                    String tripCurrency = trip.getCurrency() != null ? trip.getCurrency() : displayCurrency;

                    try {
                        double rate = exchangeService.getExchangeRate(tripCurrency, displayCurrency).join();
                        totalGlobalSpent += (tripSpentRaw * rate);
                    } catch (Exception e) {
                        totalGlobalSpent += tripSpentRaw;
                    }
                }

                final String fStatus = activeTripStatus;
                final double fSpent = totalGlobalSpent;
                final String fSymbol = symbol;

                Platform.runLater(() -> {
                    if (totalTripsLabel != null)
                        totalTripsLabel.setText(String.valueOf(totalTrips));
                    if (completedTripsLabel != null)
                        completedTripsLabel.setText(String.valueOf(completedTrips));
                    if (destinationsLabel != null)
                        destinationsLabel.setText(String.valueOf(totalDestinations));

                    if (plannedTripsLabel != null) {
                        plannedTripsLabel.setText(fStatus);
                        plannedTripsLabel.setStyle(plannedTripsLabel.getStyle() + "-fx-font-size: 24px;");
                    }

                    if (totalBudgetLabel != null) {
                        totalBudgetLabel.setText(String.format("%s%.2f", fSymbol, fSpent));
                    }
                });

            } catch (Exception e) {
            }
        });
    }

    private int repoCountCompletedTrips() {
        try {
            return tripRepo.countCompletedTrips();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getCurrencySymbol(String code) {
        try {
            return java.util.Currency.getInstance(code).getSymbol();
        } catch (Exception e) {
            return code;
        }
    }
}
