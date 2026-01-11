package com.isep.smarttripplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherController {
    @FXML
    private Label cityLabel;
    @FXML
    private ImageView weatherIconView;
    @FXML
    private Label tempLabel;
    @FXML
    private Label descLabel;
    @FXML
    private Label feelsLikeLabel;
    @FXML
    private Label updateTimeLabel;
    @FXML
    private Label windLabel;
    @FXML
    private Label humidityLabel;
    @FXML
    private javafx.scene.layout.HBox forecastContainer;

    private static String city;

    private static double latitude;
    private static double longitude;

    private static com.isep.smarttripplanner.model.Trip currentTrip;

    public static void setTrip(com.isep.smarttripplanner.model.Trip trip) {
        WeatherController.currentTrip = trip;
    }

    public static void setData(String city, double latitude, double longitude) {
        WeatherController.city = city;
        WeatherController.latitude = latitude;
        WeatherController.longitude = longitude;
        WeatherController.currentTrip = null;
    }

    @FXML
    public void initialize() {
        if (updateTimeLabel != null) {
            updateTimeLabel.setText(
                    "Updated on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        if (currentTrip != null) {
            loadMultiCityForecast();
        } else {
            loadSingleCityForecast();
        }
    }

    private void loadSingleCityForecast() {
        if (cityLabel != null && city != null)
            cityLabel.setText(city);

        com.isep.smarttripplanner.service.IWeatherService service = new com.isep.smarttripplanner.service.OpenMeteoService();
        service.getForecast(latitude, longitude).thenAccept(w -> {
            javafx.application.Platform.runLater(() -> {
                updateCurrentWeatherUI(w);
                loadForecast(w.getDailyForecasts());
            });
        });
    }

    private void loadMultiCityForecast() {
        if (currentTrip.getDestinations() == null || currentTrip.getDestinations().isEmpty()) {
            if (cityLabel != null)
                cityLabel.setText(currentTrip.getTitle());
            return;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.Map<java.time.LocalDate, com.isep.smarttripplanner.model.Destination> dayToDest = new java.util.HashMap<>();
        java.util.Set<com.isep.smarttripplanner.model.Destination> neededDestinations = new java.util.HashSet<>();

        for (int i = 0; i < 7; i++) {
            java.time.LocalDate date = today.plusDays(i);
            com.isep.smarttripplanner.model.Destination target = getDestinationForDate(date);

            if (target == null && !currentTrip.getDestinations().isEmpty()) {
                target = currentTrip.getDestinations().get(0);
            }

            if (target != null) {
                dayToDest.put(date, target);
                neededDestinations.add(target);
            }
        }

        java.util.Map<Integer, com.isep.smarttripplanner.model.WeatherData> destIdToData = new java.util.HashMap<>();
        java.util.List<java.util.concurrent.CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        com.isep.smarttripplanner.service.IWeatherService service = new com.isep.smarttripplanner.service.OpenMeteoService();

        for (com.isep.smarttripplanner.model.Destination d : neededDestinations) {
            futures.add(service.getForecast(d.getLatitude(), d.getLongitude()).thenAccept(w -> {
                synchronized (destIdToData) {
                    destIdToData.put(d.getId(), w);
                }
            }));
        }

        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0]))
                .thenRun(() -> {
                    javafx.application.Platform.runLater(() -> {
                        java.util.List<com.isep.smarttripplanner.model.WeatherData.DailyForecast> compositeForecasts = new java.util.ArrayList<>();

                        com.isep.smarttripplanner.model.Destination todayDest = dayToDest.get(today);
                        if (todayDest != null && destIdToData.containsKey(todayDest.getId())) {
                            com.isep.smarttripplanner.model.WeatherData w = destIdToData.get(todayDest.getId());
                            updateCurrentWeatherUI(w);
                            if (cityLabel != null)
                                cityLabel.setText(todayDest.getName());
                        } else if (cityLabel != null) {
                            cityLabel.setText(currentTrip.getTitle());
                        }

                        for (int i = 0; i < 7; i++) {
                            java.time.LocalDate date = today.plusDays(i);
                            com.isep.smarttripplanner.model.Destination d = dayToDest.get(date);

                            if (d != null && destIdToData.containsKey(d.getId())) {
                                com.isep.smarttripplanner.model.WeatherData w = destIdToData.get(d.getId());
                                if (w.getDailyForecasts().size() > i) {
                                    com.isep.smarttripplanner.model.WeatherData.DailyForecast df = w.getDailyForecasts()
                                            .get(i);

                                    df = new com.isep.smarttripplanner.model.WeatherData.DailyForecast(
                                            df.getDay() + "\n"
                                                    + d.getName().substring(0, Math.min(d.getName().length(), 8)),
                                            df.getMaxTemp(),
                                            df.getMinTemp(),
                                            df.getIconUrl(),
                                            df.getChanceOfRain());

                                    compositeForecasts.add(df);
                                }
                            }
                        }
                        loadForecast(compositeForecasts);
                    });
                });
    }

    private com.isep.smarttripplanner.model.Destination getDestinationForDate(java.time.LocalDate date) {
        if (currentTrip == null || currentTrip.getDestinations() == null)
            return null;

        for (com.isep.smarttripplanner.model.Destination d : currentTrip.getDestinations()) {
            if (d.getDestinationStartDate() != null && d.getDestinationEndDate() != null) {
                if ((date.isEqual(d.getDestinationStartDate()) || date.isAfter(d.getDestinationStartDate())) &&
                        (date.isEqual(d.getDestinationEndDate()) || date.isBefore(d.getDestinationEndDate()))) {
                    return d;
                }
            }
        }
        return null;
    }

    private void updateCurrentWeatherUI(com.isep.smarttripplanner.model.WeatherData w) {
        if (tempLabel != null)
            tempLabel.setText(String.format("%.0f°C", w.getTemperature()));
        if (descLabel != null)
            descLabel.setText(capitalizeFirst(w.getDescription()));
        if (feelsLikeLabel != null)
            feelsLikeLabel.setText("Feels like " + String.format("%.0f°C", w.getTemperature()));
        if (windLabel != null)
            windLabel.setText(w.getWindSpeed() + " km/h");
        if (humidityLabel != null)
            humidityLabel.setText(w.getHumidity() + "%");

        String icon = "https://cdn-icons-png.flaticon.com/512/1163/1163657.png";
        if (w.getDescription().toLowerCase().contains("rain"))
            icon = "https://cdn-icons-png.flaticon.com/512/1163/1163624.png";
        else if (w.getDescription().toLowerCase().contains("cloud"))
            icon = "https://cdn-icons-png.flaticon.com/512/1163/1163661.png";

        if (weatherIconView != null) {
            try {
                weatherIconView.setImage(new Image(icon));
            } catch (Exception e) {

            }
        }
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty())
            return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void loadForecast(java.util.List<com.isep.smarttripplanner.model.WeatherData.DailyForecast> forecasts) {
        if (forecastContainer == null || forecasts == null)
            return;

        forecastContainer.getChildren().clear();

        for (int i = 0; i < forecasts.size(); i++) {
            var f = forecasts.get(i);

            javafx.scene.layout.VBox item = new javafx.scene.layout.VBox(6);
            item.getStyleClass().add("forecast-item");
            if (i == 0) {
                item.getStyleClass().add("forecast-item-today");
            }
            item.setAlignment(javafx.geometry.Pos.CENTER);
            item.setMinWidth(110);

            Label dayLabel = new Label(f.getDay());
            dayLabel.getStyleClass().add("forecast-day");
            dayLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            ImageView iconView = new ImageView(new Image(f.getIconUrl()));
            iconView.setFitWidth(40);
            iconView.setFitHeight(40);

            javafx.scene.layout.HBox tempBox = new javafx.scene.layout.HBox(4);
            tempBox.setAlignment(javafx.geometry.Pos.CENTER);

            Label tempHighLabel = new Label(String.format("%.0f°", f.getMaxTemp()));
            tempHighLabel.getStyleClass().add("forecast-temp");

            Label tempLowLabel = new Label("/ " + String.format("%.0f°", f.getMinTemp()));
            tempLowLabel.getStyleClass().add("forecast-temp-low");

            tempBox.getChildren().addAll(tempHighLabel, tempLowLabel);

            item.getChildren().addAll(dayLabel, iconView, tempBox);

            if (f.getChanceOfRain() > 0) {
                Label chanceLabel = new Label("💧 " + f.getChanceOfRain() + "%");
                chanceLabel.getStyleClass().add("forecast-chance");
                item.getChildren().add(chanceLabel);
            }

            forecastContainer.getChildren().add(item);
        }
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
