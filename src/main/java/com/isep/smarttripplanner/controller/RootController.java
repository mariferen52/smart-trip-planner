package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.MainApplication;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;

import java.io.IOException;
import com.isep.smarttripplanner.repository.TripRepository;
import com.isep.smarttripplanner.model.Trip;

public class RootController {

    private Stage primaryStage;

    private double xOffset = 0;
    private double yOffset = 0;

    private boolean isExpanded;

    private double fontSize;

    private AnchorPane cachedHomeView;
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
    private Label home;

    @FXML
    private Region homeIcon;

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

    private static RootController instance;

    public static RootController getInstance() {
        return instance;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() throws IOException {
        instance = this;
        this.isExpanded = false;
        rootLayout.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateFontSize(newVal.doubleValue());
        });

        StackPane.setAlignment(mainContent, javafx.geometry.Pos.CENTER_LEFT);

        StackPane.setMargin(mainContent, new Insets(0, 0, 0, sidebar.getWidth()));

        sidebar.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!isExpanded) {
                StackPane.setMargin(mainContent, new Insets(0, 0, 0, newVal.doubleValue()));
            }
        });
        homeView();
    }

    private void homeView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/home-view.fxml"));
        cachedHomeView = fxmlLoader.load();
        TripController controller = fxmlLoader.getController();
        controller.getMainContent(mainContent);
        mainContent.getChildren().setAll(cachedHomeView);

        AnchorPane.setBottomAnchor(cachedHomeView, 0.0);
        AnchorPane.setTopAnchor(cachedHomeView, 0.0);
        AnchorPane.setLeftAnchor(cachedHomeView, 0.0);
        AnchorPane.setRightAnchor(cachedHomeView, 0.0);
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Node view = loader.load();

            if (view == null) {

                return;
            }

            loadView(view);

        } catch (IOException e) {
        }
    }

    public void loadView(javafx.scene.Node view) {
        mainContent.getChildren().clear();
        mainContent.getChildren().add(view);

        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }

    @FXML
    public void showProfile() {

        loadView("/com/isep/smarttripplanner/views/profile-view.fxml");
    }

    @FXML
    public void showMyTrip() {

        try {
            TripRepository repo = new TripRepository();
            Trip activeTrip = repo.findActiveTrip();

            if (activeTrip != null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/isep/smarttripplanner/views/trip-details-view.fxml"));
                javafx.scene.Node view = loader.load();

                TripDetailController controller = loader.getController();
                controller.setTrip(activeTrip);

                mainContent.getChildren().setAll(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            } else {

                changeToHomeView();
            }
        } catch (Exception e) {
            loadView("/com/isep/smarttripplanner/views/home-view.fxml");
        }
    }

    @FXML
    public void showTripHistory() {

        loadView("/com/isep/smarttripplanner/views/trip-history-view.fxml");
    }

    public void showWeatherView() {

        loadView("/com/isep/smarttripplanner/views/weather-view.fxml");
    }

    @FXML
    public void showSettings() {

        loadView("/com/isep/smarttripplanner/views/settings-view.fxml");
    }

    @FXML
    public void showTotalBudget() {

        loadView("/com/isep/smarttripplanner/views/total-budget-view.fxml");
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

        setLabelSize(home, homeIcon, fontSize);

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

        if (!isExpanded) {
            isExpanded = true;
            updateSideDrawer(sideDrawer);

            home.setContentDisplay(ContentDisplay.LEFT);
            sideDrawer.setContentDisplay(ContentDisplay.LEFT);
            profile.setContentDisplay(ContentDisplay.LEFT);
            activeTrip.setContentDisplay(ContentDisplay.LEFT);
            tripHistory.setContentDisplay(ContentDisplay.LEFT);
            totalBudget.setContentDisplay(ContentDisplay.LEFT);
            settings.setContentDisplay(ContentDisplay.LEFT);
            setScreen.setContentDisplay(ContentDisplay.LEFT);

            sideDrawer.setGraphicTextGap(sideDrawer.getWidth() / 3.2);
            home.setGraphicTextGap(profile.getWidth() / 3.2);
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

            sideDrawer.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            home.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            profile.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            activeTrip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            tripHistory.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            totalBudget.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            settings.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setScreen.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            close.setText("X");

        }
    }

    @FXML
    private void changeToHomeView() throws IOException {
        homeView();
    }
}
