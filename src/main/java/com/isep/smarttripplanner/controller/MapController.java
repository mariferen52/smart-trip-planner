package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.service.GoogleMapsAPI;
import com.isep.smarttripplanner.service.IMapService;
import com.isep.smarttripplanner.service.IWeatherService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class MapController {
    @FXML
    private WebView mapWebView;
    @FXML
    private Label weatherLabel;
    @FXML
    private Label tempLabel;
    @FXML
    private ImageView weatherIcon;
    @FXML
    private Label cityLabel;
    @FXML
    private Label areaLabel;
    @FXML
    private Label pinCodeLabel;
    @FXML
    private VBox weatherContainer;

    @FXML
    private HBox selectionOverlay;
    @FXML
    private Label selectionStatus;
    @FXML
    private Button addDestinationBtn;

    private String currentIconUrl;
    private String currentCity = "Paris"; // Default
    private String currentArea;
    private String currentPinCode;
    private static double selectedLat = 48.8566; // Static for persistence
    private static double selectedLon = 2.3522; // Static for persistence

    private final IMapService mapService = new GoogleMapsAPI();
    private final IWeatherService weatherService = new com.isep.smarttripplanner.service.OpenMeteoService();

    public void initialize() {
        System.out.println("MapController: Initializing...");

        // Navigation logic for weather explorer
        weatherContainer.setOnMouseClicked(e -> {
            if (currentIconUrl != null) {
                // Use persisted coordinates for weather view
                WeatherController.setData(currentCity, selectedLat, selectedLon);
                if (RootController.getInstance() != null) {
                    RootController.getInstance().showWeatherView();
                }
            }
        });

        // Setup JS Bridge
        mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                netscape.javascript.JSObject window = (netscape.javascript.JSObject) mapWebView.getEngine()
                        .executeScript("window");
                window.setMember("javaApp", this);
            }
        });

        loadMap(selectedLat, selectedLon); // Use persisted coordinates
    }

    // Called from JavaScript
    public void onLocationSelected(double lat, double lon) {
        javafx.application.Platform.runLater(() -> {
            selectedLat = lat;
            selectedLon = lon;
            selectionStatus.setText(String.format("Coord: %.4f, %.4f", lat, lon));
            System.out.println("Location selected: " + lat + ", " + lon);
        });
    }

    @FXML
    private void handleStartSelection() {
        selectionOverlay.setVisible(true);
        addDestinationBtn.setVisible(false);
        selectionStatus.setText("Click on map to select...");
    }

    @FXML
    private void handleConfirmSelection() {
        if (selectedLat != 0 && selectedLon != 0) {
            loadMap(selectedLat, selectedLon);
            handleCancelSelection();
        }
    }

    @FXML
    private void handleCancelSelection() {
        selectionOverlay.setVisible(false);
        addDestinationBtn.setVisible(true);
    }

    private void loadMap(double lat, double lon) {
        // Load Interactive Map
        String html = mapService.getInteractiveMapHtml(lat, lon);
        mapWebView.getEngine().loadContent(html);

        // Load Weather Async
        weatherService.getForecast(lat, lon).thenAccept(data -> {
            javafx.application.Platform.runLater(() -> {
                this.currentCity = data.getCityName();
                this.currentArea = data.getArea();
                this.currentPinCode = data.getPinCode();
                if (cityLabel != null)
                    cityLabel.setText(data.getCityName());
                if (areaLabel != null)
                    areaLabel.setText(data.getArea());
                if (pinCodeLabel != null)
                    pinCodeLabel.setText("Pincode: " + data.getPinCode());

                weatherLabel.setText(data.getDescription());
                tempLabel.setText(String.format("%.1f°C", data.getTemperature()));
                currentIconUrl = data.getIconUrl();
                if (currentIconUrl != null) {
                    try {
                        weatherIcon.setImage(new Image(currentIconUrl, true));
                    } catch (Exception e) {
                        System.err.println("Failed to load weather icon: " + currentIconUrl);
                    }
                }
            });
        }).exceptionally(ex -> {
            javafx.application.Platform.runLater(() -> weatherLabel.setText("Weather Unavailable"));
            return null;
        });
    }
}
