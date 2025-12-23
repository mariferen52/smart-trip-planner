package com.isep.smarttripplanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import javafx.scene.layout.Region;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;

public class RootController {

    private Stage primaryStage;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private BorderPane rootLayout;

    @FXML
    private Label sideDrawer;

    @FXML
    private Region sideDrawerIcon;

    @FXML
    private Label profile;

    @FXML
    private Region profileIcon;

    @FXML
    private Label activeTrip;

    @FXML
    private Region activeTripIcon;

    @FXML
    private Label tripHistory;

    @FXML
    private Region tripHistoryIcon;

    @FXML
    private Label totalBudget;

    @FXML
    private Region totalBudgetIcon;

    @FXML
    private Label settings;

    @FXML
    private Region settingsIcon;

    @FXML
    private Label setScreen;

    @FXML
    private Region setScreenIcon;

    @FXML
    private Label close;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        rootLayout.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateFontSize(newVal.doubleValue());
        });
    }

    private void setLabelSize(Label button, Region buttonIcon, double fontSize){
        double padding = fontSize / 1.5;
        buttonIcon.setStyle("-fx-min-height: " + fontSize + "px; " +
                "-fx-min-width: " + fontSize + "px; " +
                "-fx-max-height: " + fontSize + "px; " +
                "-fx-max-width: " + fontSize + "px;");
        button.setStyle("-fx-padding: " + padding + " " + (padding * 1.5) + "px;");
    }

    private void updateFontSize(double screenHeight) {
        double fontSize = screenHeight / 30.0;
        double padding = fontSize / 1.5;
        String paddingStyle = "-fx-padding: " + padding + " " + (padding * 1.5) + "px;";

        setLabelSize(sideDrawer, sideDrawerIcon,  fontSize);
        setLabelSize(profile, profileIcon, fontSize);
        profileIcon.setScaleX(1.2);
        profileIcon.setScaleY(1.2);
        setLabelSize(activeTrip, activeTripIcon, fontSize + 5);
        activeTripIcon.setScaleX(1.3);
        setLabelSize(tripHistory, tripHistoryIcon,  fontSize + 5);
        setLabelSize(totalBudget, totalBudgetIcon,  fontSize + 5);
        setLabelSize(settings, settingsIcon, fontSize);
        setLabelSize(setScreen, setScreenIcon, fontSize);

        close.setStyle("-fx-font-size: " + fontSize + "px; " + paddingStyle);
    }

    @FXML
    private void close() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void toggleScreen() {
        if (primaryStage.isFullScreen()) {
            primaryStage.setFullScreen(false);
        } else {
            primaryStage.setFullScreen(true);
        }
    }

    @FXML
    private void getScreenCoordinates(MouseEvent event) {
        xOffset = event.getX();
        yOffset = event.getY();
    }

    @FXML
    private void dragScreen(MouseEvent event) {
        if (!primaryStage.isFullScreen()) {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        }
    }
}
