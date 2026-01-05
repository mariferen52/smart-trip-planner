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

    /* Active Trip */

    @FXML
    private Label tripTitle;

    @FXML
    private VBox currencyCard;

    @FXML
    private VBox budgetCard;

    @FXML
    private VBox dayTrackerCard;

    @FXML
    private VBox activeTripContainer;

    @FXML
    private HBox widgetsContainer;

    /* Create Trip */
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
            if (currentActiveTrip != null) {
                // Show Active Trip Dashboard
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
    public void onDeleteTrip() {
        if (currentActiveTrip != null) {
            try {
                TripRepository repo = new TripRepository();
                repo.deleteTrip(currentActiveTrip.getId());

                loadDashboardData();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Could not delete trip: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void updateCard(VBox card, String value, String subtitle) {
        if (card == null)
            return;
        int index = 0;
        for (Node node : card.getChildren()) {
            if (node instanceof Label label) {
                if (index == 1) { // Value
                    label.setText(value);
                } else if (index == 2) { // Subtitle
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

        /* Active Trip HBox */
        double dashboardFontSize = newValue / 20;

        tripTitle.setStyle("-fx-font-size: " + (dashboardFontSize * 1.5) + "px; -fx-font-weight: bold;");
        if (widgetsContainer != null) {
            for (Node node : widgetsContainer.getChildren()) {
                if (node instanceof VBox widget) {
                    int index = 0;
                    for (Node child : widget.getChildren()) {
                        if (child instanceof Label label) {
                            if (index == 0) { // Title
                                label.setStyle("-fx-font-size: " + (dashboardFontSize * 0.8)
                                        + "px; -fx-font-weight: bold; -fx-text-fill: white;");
                            } else if (index == 1) { // Value
                                label.setStyle("-fx-font-size: " + (dashboardFontSize * 1.5)
                                        + "px; -fx-font-weight: bold; -fx-text-fill: white;");
                            } else { // Subtitle
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
