package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.model.TripStatus;
import com.isep.smarttripplanner.repository.TripRepository;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TripHistoryController {

    @FXML
    private VBox historyContainer;

    @FXML
    private VBox emptyStateContainer;

    @FXML
    private ScrollPane scrollContainer;

    @FXML
    private Label emptyLabel;

    @FXML
    public void initialize() {
        loadCompletedTrips();
    }

    private final com.isep.smarttripplanner.repository.AppConfigRepository configRepo = new com.isep.smarttripplanner.repository.AppConfigRepository();
    private final com.isep.smarttripplanner.service.ExchangeRateService exchangeService = new com.isep.smarttripplanner.service.ExchangeRateService();

    private void loadCompletedTrips() {
        try {
            TripRepository repo = new TripRepository();
            List<Trip> allTrips = repo.findAllTrips();

            List<Trip> completedTrips = allTrips.stream()
                    .filter(t -> t.getStatus() == TripStatus.COMPLETED)
                    .toList();

            if (completedTrips.isEmpty()) {
                if (emptyStateContainer != null) {
                    emptyStateContainer.setVisible(true);
                    emptyStateContainer.setManaged(true);
                }
                if (scrollContainer != null) {
                    scrollContainer.setVisible(false);
                    scrollContainer.setManaged(false);
                }
                return;
            }

            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(false);
                emptyStateContainer.setManaged(false);
            }
            if (scrollContainer != null) {
                scrollContainer.setVisible(true);
                scrollContainer.setManaged(true);
            }

            String userHomeCurrency = "USD";
            try {
                userHomeCurrency = configRepo.getConfig().getDefaultCurrency();
            } catch (Exception e) {
            }

            if (historyContainer != null) {
                historyContainer.getChildren().clear();

                for (int i = 0; i < completedTrips.size(); i++) {
                    Trip trip = completedTrips.get(i);
                    VBox card = createTripCard(trip, userHomeCurrency, i);
                    historyContainer.getChildren().add(card);
                }
            }

        } catch (Exception e) {
        }
    }

    private VBox createTripCard(Trip trip, String targetCurrency, int index) {
        VBox card = new VBox(10);
        card.getStyleClass().add("trip-card");
        card.getStyleClass().add("trip-card-" + (index % 5));
        card.setAlignment(Pos.CENTER_LEFT);

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label checkIcon = new Label("✅");

        Label titleLabel = new Label(trip.getTitle());
        titleLabel.getStyleClass().add("trip-card-title");

        titleRow.getChildren().addAll(checkIcon, titleLabel);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String dateRange = "";
        if (trip.getStartDate() != null && trip.getTripEndDate() != null) {
            dateRange = trip.getStartDate().format(formatter) + " → " + trip.getTripEndDate().format(formatter);
        }
        Label dateLabel = new Label("📅  " + dateRange);
        dateLabel.getStyleClass().add("trip-card-date");

        HBox statsRow = new HBox(25);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.setStyle("-fx-padding: 5 0 0 0;");

        String tripCurrency = trip.getCurrency() != null ? trip.getCurrency() : "USD";
        Label budgetLabel = new Label(String.format("💰 %s %.0f", tripCurrency, trip.getBudget()));
        budgetLabel.getStyleClass().add("trip-card-budget");

        String userHomeCurrency = "USD";
        try {
            userHomeCurrency = configRepo.getConfig().getDefaultCurrency();
        } catch (Exception e) {
        }
        final String homeCurrency = userHomeCurrency;
        if (!tripCurrency.equals(targetCurrency)) {
            if (tripCurrency.equals(homeCurrency)) {
                exchangeService.getExchangeRate(homeCurrency, targetCurrency).thenAccept(rate -> {
                    updateHistoryCardUI(budgetLabel, trip.getBudget(), rate, targetCurrency);
                }).exceptionally(ex -> null);
            } else if (targetCurrency.equals(homeCurrency)) {
                exchangeService.getExchangeRate(homeCurrency, tripCurrency).thenAccept(rateHomeToTrip -> {
                    double rate = (rateHomeToTrip == 0) ? 0 : (1.0 / rateHomeToTrip);
                    updateHistoryCardUI(budgetLabel, trip.getBudget(), rate, targetCurrency);
                }).exceptionally(ex -> null);
            } else {
                exchangeService.getExchangeRate(homeCurrency, tripCurrency).thenAccept(rateHomeToTrip -> {
                    exchangeService.getExchangeRate(homeCurrency, targetCurrency).thenAccept(rateHomeToTarget -> {
                        double rate = (rateHomeToTrip == 0) ? 0 : (rateHomeToTarget / rateHomeToTrip);
                        updateHistoryCardUI(budgetLabel, trip.getBudget(), rate, targetCurrency);
                    });
                }).exceptionally(ex -> null);
            }
        } else {
            String symbol = tripCurrency;
            try {
                symbol = java.util.Currency.getInstance(tripCurrency).getSymbol();
            } catch (Exception e) {
            }
            budgetLabel.setText(String.format("💰 %s %.2f", symbol, trip.getBudget()));
        }

        if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
            StringBuilder sb = new StringBuilder("📍 ");
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd");

            for (int i = 0; i < trip.getDestinations().size(); i++) {
                com.isep.smarttripplanner.model.Destination d = trip.getDestinations().get(i);
                sb.append(i + 1).append(". ").append(d.getName());

                if (d.getDestinationStartDate() != null && d.getDestinationEndDate() != null) {
                    sb.append(" (").append(d.getDestinationStartDate().format(dateFmt))
                            .append("-").append(d.getDestinationEndDate().format(dateFmt)).append(")");
                }

                if (i < trip.getDestinations().size() - 1) {
                    sb.append(",  ");
                }
            }

            Label destLabel = new Label(sb.toString());
            destLabel.getStyleClass().add("trip-card-destinations");
            destLabel.setWrapText(true);

            statsRow.getChildren().addAll(budgetLabel, destLabel);
        } else {
            Label destLabel = new Label("📍 No destinations");
            destLabel.getStyleClass().add("trip-card-destinations");
            statsRow.getChildren().addAll(budgetLabel, destLabel);
        }

        card.getChildren().addAll(titleRow, dateLabel, statsRow);

        return card;
    }

    private void updateHistoryCardUI(Label budgetLabel, double budgetSrc, double rate, String targetCurrency) {
        double converted = budgetSrc * rate;
        String symbol = targetCurrency;
        try {
            symbol = java.util.Currency.getInstance(targetCurrency).getSymbol();
        } catch (Exception e) {
        }
        final String finalSymbol = symbol;
        javafx.application.Platform.runLater(() -> {
            budgetLabel.setText(String.format("💰 %s %.2f", finalSymbol, converted));
        });
    }
}
