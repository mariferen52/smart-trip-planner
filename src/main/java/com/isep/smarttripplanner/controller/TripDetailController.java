package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.model.Destination;
import com.isep.smarttripplanner.service.IMapService;
import com.isep.smarttripplanner.service.MapService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import com.isep.smarttripplanner.repository.TripRepository;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;

public class TripDetailController {

    @FXML
    private javafx.scene.control.ListView<String> destinationsListView;
    @FXML
    private WebView tripMapWebView;
    @FXML
    private Label tripTitleLabel;

    @FXML
    private VBox budgetCard;
    @FXML
    private VBox weatherCard;
    @FXML
    private VBox dayTrackerCard;
    @FXML
    private VBox endDateCard;
    @FXML
    private VBox todoCard;

    private Trip trip;
    private final IMapService mapService = new MapService();

    public void setTrip(Trip trip) {
        this.trip = trip;
        if (trip != null) {
            tripTitleLabel.setText(trip.getTitle());
            initializeMap();
            updateWidgets();
            updateDestinationsList();
        }
    }

    private void initializeMap() {
        if (trip.getDestinations() == null || trip.getDestinations().isEmpty()) {
            tripMapWebView.getEngine().loadContent(mapService.getInteractiveMapHtml(48.8566, 2.3522));
            return;
        }

        Destination first = trip.getDestinations().get(0);
        tripMapWebView.getEngine()
                .loadContent(mapService.getInteractiveMapHtml(first.getLatitude(), first.getLongitude()));

        tripMapWebView.getEngine().setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        tripMapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                tripMapWebView.getEngine().executeScript("clearMap()");

                StringBuilder points = new StringBuilder("[");
                for (Destination d : trip.getDestinations()) {
                    String cleanName = d.getName().replace("'", "\\'");
                    tripMapWebView.getEngine().executeScript(
                            "addMarker(" + d.getLatitude() + ", " + d.getLongitude() + ", '" + cleanName + "')");
                    points.append("[").append(d.getLatitude()).append(",").append(d.getLongitude()).append("],");
                }

                if (points.length() > 1)
                    points.setLength(points.length() - 1);
                points.append("]");

                tripMapWebView.getEngine().executeScript("drawRoute(" + points.toString() + ")");
                tripMapWebView.getEngine().executeScript("fitBounds()");
            }
        });
    }

    private void updateDestinationsList() {
        if (destinationsListView != null && trip.getDestinations() != null) {
            destinationsListView.getItems().clear();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");

            for (int i = 0; i < trip.getDestinations().size(); i++) {
                Destination d = trip.getDestinations().get(i);
                StringBuilder sb = new StringBuilder();
                sb.append((i + 1)).append(". ").append(d.getName());

                if (d.getDestinationStartDate() != null && d.getDestinationEndDate() != null) {
                    sb.append("\n   ").append("Start: ").append(d.getDestinationStartDate().format(formatter));
                    sb.append("\n   ").append("End:   ").append(d.getDestinationEndDate().format(formatter));
                }
                destinationsListView.getItems().add(sb.toString());
            }
        }
    }

    private void updateWidgets() {
        if (trip == null)
            return;

        if (dayTrackerCard != null && dayTrackerCard.getChildren().size() >= 2) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");
            String start = trip.getStartDate().format(fmt);
            ((Label) dayTrackerCard.getChildren().get(1)).setText(start);
        }

        if (endDateCard != null && endDateCard.getChildren().size() >= 2) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");
            String end = trip.getTripEndDate().format(fmt);
            ((Label) endDateCard.getChildren().get(1)).setText(end);
        }

        if (weatherCard != null) {
            weatherCard.setOnMouseClicked(event -> handleWeatherClick());
            weatherCard.setStyle(weatherCard.getStyle() + "; -fx-cursor: hand;");

            if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
                Destination dest = trip.getDestinations().get(0);
                com.isep.smarttripplanner.service.IWeatherService weatherService = new com.isep.smarttripplanner.service.OpenMeteoService();

                weatherService.getForecast(dest.getLatitude(), dest.getLongitude()).thenAccept(w -> {
                    javafx.application.Platform.runLater(() -> {
                        if (weatherCard.getChildren().size() >= 2) {
                            String temp = String.format("%.0f°", w.getTemperature());
                            ((Label) weatherCard.getChildren().get(1)).setText(temp);
                        }
                    });
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        if (weatherCard.getChildren().size() >= 2) {
                            ((Label) weatherCard.getChildren().get(1)).setText("N/A");
                        }
                    });
                    return null;
                });
            }
        }

        if (budgetCard != null) {
            budgetCard.setOnMouseClicked(event -> handleBudgetClick());
            budgetCard.setStyle(budgetCard.getStyle() + "; -fx-cursor: hand;");

            com.isep.smarttripplanner.repository.AppConfigRepository cfgRepo = new com.isep.smarttripplanner.repository.AppConfigRepository();
            com.isep.smarttripplanner.service.ExchangeRateService exService = new com.isep.smarttripplanner.service.ExchangeRateService();

            try {
                com.isep.smarttripplanner.model.AppConfig cfg = cfgRepo.getConfig();
                String homeCurrency = cfg.getDefaultCurrency();
                String targetCurrency = cfg.getTargetCurrency();
                String tripCurrency = trip.getCurrency() != null ? trip.getCurrency() : homeCurrency;

                // Calculate amounts
                com.isep.smarttripplanner.repository.ExpenseRepository expRepo = new com.isep.smarttripplanner.repository.ExpenseRepository();
                java.util.List<com.isep.smarttripplanner.model.Expense> expenses = expRepo
                        .findExpensesByTripId(trip.getId());
                double totalSpentSrc = expenses.stream().mapToDouble(com.isep.smarttripplanner.model.Expense::getAmount)
                        .sum();
                double remainingSrc = trip.getBudget() - totalSpentSrc;

                if (tripCurrency.equals(targetCurrency)) {
                    updateDetailBudgetUI(trip.getBudget(), remainingSrc, 1.0, targetCurrency);
                } else if (tripCurrency.equals(homeCurrency)) {
                    // Trip is Home. Rate = Home -> Target
                    exService.getExchangeRate(homeCurrency, targetCurrency).thenAccept(rate -> {
                        updateDetailBudgetUI(trip.getBudget(), remainingSrc, rate, targetCurrency);
                    }).exceptionally(ex -> null);
                } else if (targetCurrency.equals(homeCurrency)) {
                    // Target is Home. Rate = Trip -> Home = 1 / (Home -> Trip)
                    exService.getExchangeRate(homeCurrency, tripCurrency).thenAccept(rateHomeToTrip -> {
                        double rate = (rateHomeToTrip == 0) ? 0 : (1.0 / rateHomeToTrip);
                        updateDetailBudgetUI(trip.getBudget(), remainingSrc, rate, targetCurrency);
                    }).exceptionally(ex -> null);
                } else {
                    // Full Triangulation: Trip -> [Home] -> Target
                    exService.getExchangeRate(homeCurrency, tripCurrency).thenAccept(rateHomeToTrip -> {
                        exService.getExchangeRate(homeCurrency, targetCurrency).thenAccept(rateHomeToTarget -> {
                            double rate = (rateHomeToTrip == 0) ? 0 : (rateHomeToTarget / rateHomeToTrip);
                            updateDetailBudgetUI(trip.getBudget(), remainingSrc, rate, targetCurrency);
                        });
                    }).exceptionally(ex -> null);
                }

            } catch (Exception e) {
            }
        }

        if (todoCard != null) {
            todoCard.setOnMouseClicked(event -> handleTodoClick());
        }

        if (dayTrackerCard != null) {
            dayTrackerCard.setOnMouseClicked(event -> handleDailySummary());
        }
        if (endDateCard != null) {
            endDateCard.setOnMouseClicked(event -> handleDailySummary());
        }
    }

    private void handleDailySummary() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/isep/smarttripplanner/views/daily-summary-view.fxml"));
            javafx.scene.Node view = loader.load();

            com.isep.smarttripplanner.controller.SummaryController controller = loader.getController();
            controller.initData(trip, false);

            com.isep.smarttripplanner.controller.RootController.getInstance().loadView(view);
        } catch (java.io.IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to load Daily Summary view: " + e.getMessage());
            alert.show();
        }
    }

    private void handleBudgetClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/isep/smarttripplanner/views/budget-view.fxml"));
            javafx.scene.Node view = loader.load();

            com.isep.smarttripplanner.controller.BudgetController controller = loader.getController();
            controller.initData(trip);

            com.isep.smarttripplanner.controller.RootController.getInstance().loadView(view);
        } catch (java.io.IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to load Budget view: " + e.getMessage());
            alert.show();
        }
    }

    private void handleTodoClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/isep/smarttripplanner/views/todo-view.fxml"));
            javafx.scene.Node view = loader.load();

            com.isep.smarttripplanner.controller.TodoController controller = loader.getController();
            controller.initData(trip);

            com.isep.smarttripplanner.controller.RootController.getInstance().loadView(view);
        } catch (java.io.IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to load Todo view: " + e.getMessage());
            alert.show();
        }
    }

    private void handleWeatherClick() {
        try {
            if (trip != null && trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
                com.isep.smarttripplanner.controller.WeatherController.setTrip(trip);
            }

            com.isep.smarttripplanner.controller.RootController.getInstance()
                    .loadView("/com/isep/smarttripplanner/views/weather-view.fxml");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Could not load Weather View: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void handleDeleteTrip() {
        if (trip == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Trip");
        alert.setHeaderText("Delete '" + trip.getTitle() + "'?");
        alert.setContentText("Are you sure you want to delete this trip? This cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                TripRepository repo = new TripRepository();
                repo.deleteTrip(trip.getId());

                handleBack();

            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("Failed to delete trip: " + e.getMessage());
                error.show();
            }
        }
    }

    @FXML
    private void handleCompleteTrip() {
        if (trip == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Complete Trip");
        alert.setHeaderText("Mark '" + trip.getTitle() + "' as Complete?");
        alert.setContentText("This will move the trip to your history.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                TripRepository repo = new TripRepository();
                trip.setStatus(com.isep.smarttripplanner.model.TripStatus.COMPLETED);
                repo.updateTrip(trip);

                handleBack();

            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("Failed to complete trip: " + e.getMessage());
                error.show();
            }
        }
    }

    @FXML
    private void handleEditTrip() {
        if (trip == null)
            return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/isep/smarttripplanner/views/trip-creation-view.fxml"));
            javafx.scene.Node view = loader.load();

            TripCreationController controller = loader.getController();
            controller.setTrip(trip);

            com.isep.smarttripplanner.controller.RootController.getInstance().loadView(view);

        } catch (java.io.IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to load Edit Trip view: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private javafx.scene.layout.AnchorPane rootPane;
    @FXML
    private VBox sidebar;
    @FXML
    private javafx.scene.layout.HBox buttonContainer;
    @FXML
    private Label tripOverviewLabel;
    @FXML
    private Label destinationsLabel;
    @FXML
    private javafx.scene.layout.FlowPane cardsContainer;

    @FXML
    private void initialize() {
        if (rootPane != null) {
            rootPane.widthProperty().addListener((obs, oldVal, newVal) -> updateLayout());
            rootPane.heightProperty().addListener((obs, oldVal, newVal) -> updateLayout());
        }
    }

    private void updateLayout() {
        if (rootPane == null)
            return;

        double width = rootPane.getWidth();
        double height = rootPane.getHeight();
        if (width == 0 || height == 0)
            return;

        double baseFontSize = Math.min(width, height) / 40;
        double titleFontSize = baseFontSize * 1.5;
        double subHeaderSize = baseFontSize * 1.0;
        double normalTextSize = baseFontSize * 0.8;

        double sidebarRatio = 0.45;
        if (sidebar != null) {
            sidebar.setPrefWidth(width * sidebarRatio);
        }

        if (tripTitleLabel != null) {
            tripTitleLabel.setStyle("-fx-font-size: " + titleFontSize + "px; -fx-font-weight: bold;");
        }

        String subHeaderStyle = "-fx-font-size: " + subHeaderSize + "px; -fx-font-weight: bold; -fx-text-fill: grey;";
        if (tripOverviewLabel != null)
            tripOverviewLabel.setStyle(subHeaderStyle);
        if (destinationsLabel != null)
            destinationsLabel.setStyle("-fx-font-size: " + subHeaderSize + "px; -fx-font-weight: bold;");

        if (buttonContainer != null) {
            buttonContainer.getChildren().forEach(node -> {
                if (node instanceof javafx.scene.control.Button) {
                    node.setStyle("-fx-cursor: hand; -fx-font-size: " + normalTextSize + "px; -fx-padding: "
                            + (normalTextSize / 2) + " " + normalTextSize + "; " +
                            (node.getStyle().contains("-fx-text-fill: red")
                                    ? "-fx-background-color: #ffe6e6; -fx-text-fill: red;"
                                    : "-fx-background-color: #e0e0e0;"));
                }
            });
        }

        double availableWidth = (width * sidebarRatio) - 60;

        double cardSize = (availableWidth / 2.0) * 0.65;

        if (cardSize < 100)
            cardSize = 100;

        setCardSize(budgetCard, cardSize);
        if (budgetCard != null) {
            budgetCard.setPrefWidth(cardSize * 1.95);
        }

        setCardSize(dayTrackerCard, cardSize);
        setCardSize(weatherCard, cardSize);
        setCardSize(endDateCard, cardSize);

        if (cardsContainer != null) {
            cardsContainer.setPrefWrapLength(width * sidebarRatio);
        }

        updateCardStyles(budgetCard, normalTextSize);
        updateCardStyles(dayTrackerCard, normalTextSize);
        updateCardStyles(weatherCard, normalTextSize);
        updateCardStyles(endDateCard, normalTextSize);

        if (destinationsListView != null) {
            destinationsListView.setStyle("-fx-font-size: " + normalTextSize
                    + "px; -fx-background-color: transparent; -fx-control-inner-background: #f9f9f9;");
        }
    }

    private void setCardSize(VBox card, double size) {
        if (card != null) {
            card.setPrefWidth(size);
            card.setPrefHeight(size * 0.8);
        }
    }

    private void updateCardStyles(VBox card, double fontSize) {
        if (card == null)
            return;
        if (card.getChildren().size() > 0 && card.getChildren().get(0) instanceof Label title) {
            title.setStyle("-fx-font-weight: bold; -fx-font-size: " + (fontSize * 0.9) + "px; -fx-text-fill: white;");
        }
        if (card.getChildren().size() > 1 && card.getChildren().get(1) instanceof Label value) {
            value.setStyle("-fx-font-weight: bold; -fx-font-size: " + (fontSize * 1.4) + "px; -fx-text-fill: white;");
        }
    }

    private void updateDetailBudgetUI(double budgetSrc, double remainingSrc, double rate, String targetCurrency) {
        javafx.application.Platform.runLater(() -> {
            if (budgetCard != null && budgetCard.getChildren().size() >= 2) {
                double convertedRemaining = remainingSrc * rate;
                double convertedTotal = budgetSrc * rate;

                String sym = targetCurrency;
                try {
                    sym = java.util.Currency.getInstance(targetCurrency).getSymbol();
                } catch (Exception e) {
                }
                ((Label) budgetCard.getChildren().get(1)).setText(
                        String.format("Rem: %s%.0f / Tot: %s%.0f", sym, convertedRemaining, sym, convertedTotal));
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            com.isep.smarttripplanner.controller.RootController.getInstance()
                    .loadView("/com/isep/smarttripplanner/views/home-view.fxml");
        } catch (Exception e) {
        }
    }
}
