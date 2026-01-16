package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Destination;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import java.time.LocalDate;
import com.isep.smarttripplanner.service.*;

public class AddDestinationController {

    @FXML
    private TextField destinationName;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    @FXML
    private WebView mapWebView;

    private double selectedLat = 48.8566;
    private double selectedLon = 2.3522;

    private final com.isep.smarttripplanner.service.IMapService mapService = new com.isep.smarttripplanner.service.MapService();
    private final com.isep.smarttripplanner.service.IWeatherService weatherService = new com.isep.smarttripplanner.service.OpenMeteoService();

    private java.util.List<javafx.util.Pair<LocalDate, LocalDate>> blockedRanges = new java.util.ArrayList<>();

    public void setBlockedRanges(java.util.List<javafx.util.Pair<LocalDate, LocalDate>> ranges) {
        this.blockedRanges = ranges;
        setupDateCellFactory();
    }

    @FXML
    public void initialize() {
        mapWebView.getEngine().setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        String html = mapService.getInteractiveMapHtml(selectedLat, selectedLon, 1);
        mapWebView.getEngine().loadContent(html);

        mapWebView.getEngine().getLoadWorker().exceptionProperty().addListener((obs, oldExc, newExc) -> {
            if (newExc != null) {
                newExc.printStackTrace();
            }
        });

        setupDateCellFactory();
        setupCityAutoLocation();

        mapWebView.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            String script = "try {" +
                    "  var p = L.point(" + e.getX() + ", " + e.getY() + ");" +
                    "  var coord = window.map.containerPointToLatLng(p);" +
                    "  if(window.selectionMarker) { window.selectionMarker.setLatLng(coord); }" +
                    "  else { window.selectionMarker = L.marker(coord).addTo(window.map); }" +
                    "  if(window.javaApp) window.javaApp.onLocationSelected(coord.lat, coord.lng);" +
                    "} catch(err) { }";

            mapWebView.getEngine().executeScript(script);
        });

        mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                netscape.javascript.JSObject window = (netscape.javascript.JSObject) mapWebView.getEngine()
                        .executeScript("window");
                window.setMember("javaApp", this);
            }
        });
    }

    private void setupCityAutoLocation() {
        destinationName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                triggerGeocoding(destinationName.getText());
            }
        });

        destinationName.setOnAction(e -> triggerGeocoding(destinationName.getText()));
    }

    private void triggerGeocoding(String city) {
        if (city == null || city.trim().isEmpty())
            return;

        weatherService.getCoordinates(city).thenAccept(coords -> {
            if (coords != null) {
                javafx.application.Platform.runLater(() -> {
                    this.selectedLat = coords[0];
                    this.selectedLon = coords[1];

                    String script = String.format(
                            "window.map.setView([%f, %f], 10);" +
                                    "if(window.selectionMarker) { window.selectionMarker.setLatLng([%f, %f]); }" +
                                    "else { window.selectionMarker = L.marker([%f, %f]).addTo(window.map); }",
                            selectedLat, selectedLon, selectedLat, selectedLon, selectedLat, selectedLon);
                    mapWebView.getEngine().executeScript(script);

                });
            }
        }).exceptionally(ex -> {
            return null;
        });
    }

    private void setupDateCellFactory() {
        Callback<DatePicker, DateCell> dayCellFactory = picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }

                if (blockedRanges != null) {
                    for (javafx.util.Pair<LocalDate, LocalDate> range : blockedRanges) {
                        if (range.getKey() != null && range.getValue() != null) {
                            if (!date.isBefore(range.getKey()) && !date.isAfter(range.getValue())) {
                                setDisable(true);
                                setStyle("-fx-background-color: #ffcc00;");
                                setTooltip(new javafx.scene.control.Tooltip("Date occupied by another trip"));
                            }
                        }
                    }
                }
            }
        };

        startDate.setDayCellFactory(dayCellFactory);
        endDate.setDayCellFactory(dayCellFactory);
    }

    public void log(String message) {
    }

    public void onLocationSelected(double lat, double lon) {
        this.selectedLat = lat;
        this.selectedLon = lon;

        weatherService.getCityName(lat, lon).thenAccept(name -> {
            javafx.application.Platform.runLater(() -> {
                if (name != null && !name.equals("Unknown Location")) {
                    destinationName.setText(name);
                }
            });
        });
    }

    public Destination getDestination() {
        String name = destinationName.getText();
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        boolean valid = true;
        if (startDate.getValue() == null) {
            startDate.setStyle("-fx-border-color: red;");
            valid = false;
        } else {
            startDate.setStyle("");
        }

        if (endDate.getValue() == null) {
            endDate.setStyle("-fx-border-color: red;");
            valid = false;
        } else {
            endDate.setStyle("");
        }

        if (!valid)
            return null;

        if (endDate.getValue().isBefore(startDate.getValue())) {
            endDate.setStyle("-fx-border-color: red;");
            return null;
        } else {
            endDate.setStyle("");
        }

        Destination dest = new Destination(name, selectedLat, selectedLon);
        dest.setDestinationStartDate(startDate.getValue());
        dest.setDestinationEndDate(endDate.getValue());
        return dest;
    }
}
