package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.MainApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.repository.TripRepository;

import java.io.IOException;

public class TripController {

    @FXML
    private AnchorPane mainContent;

    @FXML
    private Label tripTitle;

    @FXML
    private VBox currencyCard;

    @FXML
    private VBox budgetCard;

    @FXML
    private VBox dayTrackerCard;

    @FXML
    private VBox weatherCard;

    private final com.isep.smarttripplanner.service.IWeatherService weatherService = new com.isep.smarttripplanner.service.OpenMeteoService();
    @FXML
    private javafx.scene.web.WebView tripMapWebView;

    @FXML
    private VBox activeTripContainer;

    @FXML
    private HBox widgetsContainer;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label createTripButton;

    @FXML
    private Region createTripButtonIcon;

    @FXML
    private AnchorPane homeView;

    @FXML
    private void initialize() {
        homeView.heightProperty().addListener(((observable, oldValue, newValue) -> {
            updateFontSize(newValue.doubleValue());
            StackPane.setMargin(activeTripContainer, new Insets(0, newValue.doubleValue() / 30,
                    newValue.doubleValue() / 30, newValue.doubleValue() / 30));
        }));

        loadDashboardData();
    }

    private Trip currentActiveTrip;

    private void loadDashboardData() {
        TripRepository repo = new TripRepository();
        try {
            this.currentActiveTrip = repo.findActiveTrip();
            System.out.println("TripController: loadDashboardData called. Active Trip found: "
                    + (currentActiveTrip != null ? currentActiveTrip.getTitle() : "NONE"));

            if (currentActiveTrip != null) {
                activeTripContainer.setVisible(true);
                activeTripContainer.setManaged(true);
                welcomeLabel.setVisible(false);
                welcomeLabel.setManaged(false);
                createTripButton.setVisible(false);
                createTripButton.setManaged(false);

                tripTitle.setText(currentActiveTrip.getTitle());

                updateCard(budgetCard, String.format("$%.2f", currentActiveTrip.getBudget()), "Total");

                java.time.LocalDate now = java.time.LocalDate.now();
                java.time.LocalDate start = currentActiveTrip.getStartDate();
                java.time.LocalDate end = currentActiveTrip.getTripEndDate();

                String dayValue;
                String daySubtitle;

                if (now.isBefore(start)) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(now, start);
                    dayValue = String.valueOf(days);
                    daySubtitle = "Days Until Start";
                } else if (now.isAfter(end)) {
                    dayValue = "0";
                    daySubtitle = "Trip Ended";
                } else {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(now, end);
                    dayValue = String.valueOf(days);
                    daySubtitle = "Days Left";
                }
                updateCard(dayTrackerCard, dayValue, daySubtitle);

                if (currentActiveTrip.getDestinations() != null && !currentActiveTrip.getDestinations().isEmpty()) {
                    weatherCard.setOnMouseClicked(event -> {
                        try {
                            if (currentActiveTrip != null && !currentActiveTrip.getDestinations().isEmpty()) {
                                com.isep.smarttripplanner.controller.WeatherController.setTrip(currentActiveTrip);
                            }
                            com.isep.smarttripplanner.controller.RootController.getInstance()
                                    .loadView("/com/isep/smarttripplanner/views/weather-view.fxml");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    weatherCard.setStyle(weatherCard.getStyle() + "; -fx-cursor: hand;");

                    com.isep.smarttripplanner.model.Destination firstDest = currentActiveTrip.getDestinations().get(0);
                    weatherService.getForecast(firstDest.getLatitude(), firstDest.getLongitude())
                            .thenAccept(weather -> {
                                javafx.application.Platform.runLater(() -> {
                                    String temp = String.format("%.0f°", weather.getTemperature());
                                    String desc = weather.getDescription();
                                    if (desc != null && !desc.isEmpty()) {
                                        desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
                                    }
                                    System.out.println("DEBUG: Weather updated on Dashboard: " + temp + ", " + desc);
                                    updateCard(weatherCard, temp, desc);
                                });
                            }).exceptionally(ex -> {
                                ex.printStackTrace();
                                javafx.application.Platform.runLater(() -> {
                                    updateCard(weatherCard, "--", "Error");
                                });
                                return null;
                            });
                } else {
                    updateCard(weatherCard, "--", "No Dest.");
                }

            } else {
                activeTripContainer.setVisible(false);
                activeTripContainer.setManaged(false);
                welcomeLabel.setVisible(true);
                welcomeLabel.setManaged(true);
                createTripButton.setVisible(true);
                createTripButton.setManaged(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onCompleteTrip() {
        if (currentActiveTrip != null) {
            try {
                TripRepository repo = new TripRepository();
                currentActiveTrip.setStatus(com.isep.smarttripplanner.model.TripStatus.COMPLETED);
                repo.updateTrip(currentActiveTrip);
                System.out.println("Trip marked as COMPLETED: " + currentActiveTrip.getId());

                loadDashboardData();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Could not complete trip: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void openTripDetails() {
        System.out.println("DEBUG: openTripDetails CLICKED!");
        try {
            System.out.println("DEBUG: Loading trip-details-view.fxml...");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/isep/smarttripplanner/views/trip-details-view.fxml"));
            javafx.scene.Node view = loader.load();

            com.isep.smarttripplanner.controller.TripDetailController controller = loader.getController();
            controller.setTrip(currentActiveTrip);

            System.out.println("DEBUG: Switching View...");
            homeView.getChildren().setAll(view);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCard(VBox card, String value, String subtitle) {
        if (card == null)
            return;
        int index = 0;
        for (Node node : card.getChildren()) {
            if (node instanceof Label label) {
                if (index == 1) {
                    label.setText(value);
                } else if (index == 2) {
                    label.setText(subtitle);
                }
                index++;
            }
        }
    }

    @FXML
    protected void getMainContent(AnchorPane mainContent) {
        this.mainContent = mainContent;
    }

    private void setLabelSize(Parent label, double fontSize) {
        label.setStyle("-fx-font-size: " + fontSize + "px; " + " -fx-padding: " + fontSize + "px;"
                + "-fx-graphic-text-gap: " + fontSize / 2.3 + "px; ");
    }

    private void setIconSize(Parent label, double fontSize) {
        label.setStyle("-fx-min-width: " + fontSize + "px; " +
                "    -fx-min-height: " + fontSize + "px; " +
                "    -fx-max-width:" + fontSize + "px; " +
                "    -fx-max-height: " + fontSize + "px; ");
    }

    private void updateFontSize(double newValue) {
        double largeFontSize = newValue / 10;
        double padding = largeFontSize;
        String pad = "-fx-padding: 0 " + padding + " 0 0 ;";

        setLabelSize(welcomeLabel, largeFontSize);
        setLabelSize(createTripButton, largeFontSize);
        createTripButton.getStyleClass().add(pad);
        setIconSize(createTripButtonIcon, largeFontSize * 1.2);

        double dashboardFontSize = newValue / 20;

        tripTitle.setStyle("-fx-font-size: " + (dashboardFontSize * 1.5) + "px; -fx-font-weight: bold;");
        if (widgetsContainer != null) {
            for (Node node : widgetsContainer.getChildren()) {
                if (node instanceof VBox widget) {
                    int index = 0;
                    for (Node child : widget.getChildren()) {
                        if (child instanceof Label label) {
                            if (index == 0) {
                                label.setStyle("-fx-font-size: " + (dashboardFontSize * 0.8)
                                        + "px; -fx-font-weight: bold; -fx-text-fill: white;");
                            } else if (index == 1) {
                                label.setStyle("-fx-font-size: " + (dashboardFontSize * 1.5)
                                        + "px; -fx-font-weight: bold; -fx-text-fill: white;");
                            } else {
                                label.setStyle(
                                        "-fx-font-size: " + (dashboardFontSize * 0.6) + "px; -fx-text-fill: white;");
                            }
                            index++;
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void changeToTripCreationView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApplication.class.getResource("/com/isep/smarttripplanner/views/trip-creation-view.fxml"));
        AnchorPane creationView = fxmlLoader.load();

        if (homeView.getParent() instanceof AnchorPane parent) {
            parent.getChildren().setAll(creationView);
            AnchorPane.setTopAnchor(creationView, 0.0);
            AnchorPane.setBottomAnchor(creationView, 0.0);
            AnchorPane.setLeftAnchor(creationView, 0.0);
            AnchorPane.setRightAnchor(creationView, 0.0);
        } else {
            System.err.println("CRITICAL: homeView has no parent or parent is not an AnchorPane!");
        }
    }

}
