package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.service.MapService;
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
    private String currentCity = "Paris";

    private static double selectedLat = 48.8566;
    private static double selectedLon = 2.3522;

    private final IMapService mapService = new MapService();
    private final IWeatherService weatherService = new com.isep.smarttripplanner.service.OpenMeteoService();

    public void initialize() {
        mapWebView.getEngine().setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        weatherContainer.setOnMouseClicked(e -> {
            if (currentIconUrl != null) {
                WeatherController.setData(currentCity, selectedLat, selectedLon);
                if (RootController.getInstance() != null) {
                    RootController.getInstance().showWeatherView();
                }
            }
        });

        mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                netscape.javascript.JSObject window = (netscape.javascript.JSObject) mapWebView.getEngine()
                        .executeScript("window");
                window.setMember("javaApp", this);

                mapWebView.getEngine().executeScript("console.log = function(message) { javaApp.log(message); }");
            }
        });

        loadMap(selectedLat, selectedLon);
    }

    public void onLocationSelected(double lat, double lon) {
        javafx.application.Platform.runLater(() -> {
            selectedLat = lat;
            selectedLon = lon;
            selectionStatus.setText(String.format("Coord: %.4f, %.4f", lat, lon));
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
        String html = mapService.getInteractiveMapHtml(lat, lon, 1);
        mapWebView.getEngine().loadContent(html);

        weatherService.getForecast(lat, lon).thenAccept(data -> {
            javafx.application.Platform.runLater(() -> {
                this.currentCity = data.getCityName();

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

                    }
                }
            });
        }).exceptionally(ex -> {
            javafx.application.Platform.runLater(() -> weatherLabel.setText("Weather Unavailable"));
            return null;
        });
    }
}
