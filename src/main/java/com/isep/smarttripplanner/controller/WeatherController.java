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
    private Label visibilityLabel;
    @FXML
    private Label pressureLabel;
    @FXML
    private Label uvLabel;
    @FXML
    private javafx.scene.layout.HBox forecastContainer;

    private static String city;
    private static String area;
    private static String pincode;
    private static String iconUrl;
    private static String temp;
    private static String desc;

    public static void setData(String city, String area, String pincode, String iconUrl, String temp, String desc) {
        WeatherController.city = city;
        WeatherController.area = area;
        WeatherController.pincode = pincode;
        WeatherController.iconUrl = iconUrl;
        WeatherController.temp = temp;
        WeatherController.desc = desc;
    }

    @FXML
    public void initialize() {
        if (cityLabel != null && city != null)
            cityLabel.setText(city + ", " + (area != null ? area : ""));
        if (tempLabel != null && temp != null)
            tempLabel.setText(temp);
        if (descLabel != null && desc != null)
            descLabel.setText(capitalizeFirst(desc));
        if (feelsLikeLabel != null && temp != null) {
            feelsLikeLabel.setText("Feels like " + temp);
        }
        if (updateTimeLabel != null) {
            updateTimeLabel.setText("Updated on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " Local Time");
        }
        if (weatherIconView != null && iconUrl != null) {
            try {
                weatherIconView.setImage(new Image(iconUrl));
            } catch (Exception e) {
                System.err.println("Failed to load weather icon in WeatherController");
            }
        }
        
        // Set mock weather details
        if (windLabel != null) windLabel.setText("10 km/h ENE");
        if (humidityLabel != null) humidityLabel.setText("52%");
        if (visibilityLabel != null) visibilityLabel.setText("11.27 km");
        if (pressureLabel != null) pressureLabel.setText("1014 hPa");
        if (uvLabel != null) uvLabel.setText("1 (Low)");

        loadMockForecast();
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void loadMockForecast() {
        if (forecastContainer == null) return;

        forecastContainer.getChildren().clear();

        String[] days = {"Tonight", "Mon 24", "Tue 25", "Wed 26", "Thu 27", "Fri 28"};
        String[] tempsHigh = {"--", "24°", "22°", "20°", "23°", "25°"};
        String[] tempsLow = {"18°", "13°", "13°", "12°", "14°", "16°"};
        String[] chances = {"24%", "66%", "34%", "2%", "", ""};
        String[] icons = {"🌙", "⛅", "🌤", "☀", "⛅", "🌤"};

        for (int i = 0; i < days.length; i++) {
            javafx.scene.layout.VBox item = new javafx.scene.layout.VBox(6);
            item.getStyleClass().add("forecast-item");
            if (i == 0) {
                item.getStyleClass().add("forecast-item-today");
            }
            item.setAlignment(javafx.geometry.Pos.CENTER);

            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("forecast-day");

            Label iconLabel = new Label(icons[i]);
            iconLabel.setStyle("-fx-font-size: 32px;");
            iconLabel.getStyleClass().add("forecast-icon");

            javafx.scene.layout.HBox tempBox = new javafx.scene.layout.HBox(4);
            tempBox.setAlignment(javafx.geometry.Pos.CENTER);
            
            Label tempHighLabel = new Label(tempsHigh[i]);
            tempHighLabel.getStyleClass().add("forecast-temp");
            
            Label tempLowLabel = new Label("/ " + tempsLow[i]);
            tempLowLabel.getStyleClass().add("forecast-temp-low");
            
            tempBox.getChildren().addAll(tempHighLabel, tempLowLabel);

            item.getChildren().addAll(dayLabel, iconLabel, tempBox);
            
            if (!chances[i].isEmpty()) {
                Label chanceLabel = new Label("💧 " + chances[i]);
                chanceLabel.getStyleClass().add("forecast-chance");
                item.getChildren().add(chanceLabel);
            }

            forecastContainer.getChildren().add(item);
        }
    }
}
