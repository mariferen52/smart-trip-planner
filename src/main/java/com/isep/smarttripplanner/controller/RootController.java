package com.isep.smarttripplanner.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;

public class RootController {

    private Stage primaryStage;

    private double xOffset = 0;
    private double yOffset = 0;

    private boolean isExpanded;

    private double fontSize;

    @FXML
    private StackPane rootLayout;

    @FXML
    private AnchorPane mainContent;

    @FXML
    private VBox sidebar;

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
        this.isExpanded = false;
        rootLayout.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateFontSize(newVal.doubleValue());
        });
        StackPane.setMargin(mainContent, new Insets(0, 0, 0, 0));
        mainContent.translateXProperty().bind(sidebar.widthProperty());
        mainContent.prefWidthProperty().bind(rootLayout.widthProperty().subtract(sidebar.widthProperty()));
    }

    private void setLabelSize(Label button, Region buttonIcon, double fontSize) {
        double padding = fontSize / 1.5;
        buttonIcon.setStyle("-fx-min-height: " + fontSize + "px; " +
                "-fx-min-width: " + fontSize + "px; " +
                "-fx-max-height: " + fontSize + "px; " +
                "-fx-max-width: " + fontSize + "px;");
        button.setStyle("-fx-font-size: " + fontSize + "px; -fx-padding: " + padding + " " + (padding * 1.5) + "px;");
    }

    private void updateFontSize(double screenHeight) {
        this.fontSize = screenHeight / 30.0;
        double padding = fontSize / 1.5;
        String paddingStyle = "-fx-padding: " + padding + " " + (padding * 1.5) + "px;";

        setLabelSize(sideDrawer, sideDrawerIcon, fontSize);
        setLabelSize(profile, profileIcon, fontSize);
        profileIcon.setScaleX(1.2);
        profileIcon.setScaleY(1.2);
        setLabelSize(activeTrip, activeTripIcon, fontSize);
        activeTripIcon.setScaleX(1.2);
        activeTripIcon.setScaleX(1.44);

        setLabelSize(tripHistory, tripHistoryIcon, fontSize);
        tripHistoryIcon.setScaleX(1.2);
        tripHistoryIcon.setScaleY(1.2);

        setLabelSize(totalBudget, totalBudgetIcon, fontSize);
        totalBudgetIcon.setScaleX(1.2);
        totalBudgetIcon.setScaleY(1.2);

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
            setScreen.setText("Fullscreen");
            primaryStage.setFullScreen(false);
        } else {
            setScreen.setText("Restore");
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

    private void updateSideDrawer(Label sideDrawer) {
        String bg = isExpanded ? "#000000" : "#ffffff";
        String text = isExpanded ? "#ffffff" : "#000000";
        double padding = fontSize / 1.5;
        sideDrawer.setStyle("-fx-font-size: " + fontSize + "; -fx-background-color: " + bg +
                "; -fx-text-fill: " + text + "; -fx-background-radius: 9px 0px 0px 0px;" + "-fx-padding: " + padding
                + " " + (padding * 1.5) + "px;");
        sideDrawerIcon.setStyle("-fx-min-height: " + fontSize + "px; " +
                "-fx-min-width: " + fontSize + "px; " +
                "-fx-max-height: " + fontSize + "px; " +
                "-fx-max-width: " + fontSize + "px;" + " -fx-background-color: " + text + ";");
    }

    @FXML
    private void expandSideDrawer() {
        double padding = fontSize / 2;
        if (!isExpanded) {
            isExpanded = true;
            updateSideDrawer(sideDrawer);

            mainContent.translateXProperty().unbind();
            mainContent.prefWidthProperty().unbind();

            sideDrawer.setContentDisplay(ContentDisplay.LEFT);
            profile.setContentDisplay(ContentDisplay.LEFT);
            activeTrip.setContentDisplay(ContentDisplay.LEFT);
            tripHistory.setContentDisplay(ContentDisplay.LEFT);
            totalBudget.setContentDisplay(ContentDisplay.LEFT);
            settings.setContentDisplay(ContentDisplay.LEFT);
            setScreen.setContentDisplay(ContentDisplay.LEFT);

            sideDrawer.setGraphicTextGap(sideDrawer.getWidth() / 3.2);
            profile.setGraphicTextGap(profile.getWidth() / 3.2);
            activeTrip.setGraphicTextGap(activeTrip.getWidth() / 3.2);
            tripHistory.setGraphicTextGap(tripHistory.getWidth() / 3.2);
            totalBudget.setGraphicTextGap(totalBudget.getWidth() / 3.2);
            settings.setGraphicTextGap(settings.getWidth() / 3.2);
            setScreen.setGraphicTextGap(setScreen.getWidth() / 3.2);

            close.setText("Close");

        } else {
            isExpanded = false;
            updateSideDrawer(sideDrawer);

            mainContent.prefWidthProperty().bind(rootLayout.widthProperty().subtract(sidebar.widthProperty()));
            mainContent.translateXProperty().bind(sidebar.widthProperty());
            sideDrawer.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            profile.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            activeTrip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            tripHistory.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            totalBudget.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            settings.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setScreen.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            close.setText("X");

        }
    }
}
