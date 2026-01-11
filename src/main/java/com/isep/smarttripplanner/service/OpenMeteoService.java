package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.WeatherData;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OpenMeteoService implements IWeatherService {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public CompletableFuture<WeatherData> getForecast(double lat, double lon) {
        String weatherUrl = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,weather_code,is_day,windspeed_10m,relativehumidity_2m&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto",
                lat, lon);

        HttpRequest weatherRequest = HttpRequest.newBuilder()
                .uri(URI.create(weatherUrl))
                .header("User-Agent", "SmartTripPlanner/1.0")
                .build();

        CompletableFuture<JsonObject> weatherFuture = httpClient
                .sendAsync(weatherRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200)
                        throw new RuntimeException("Open-Meteo Error: " + response.statusCode());
                    return JsonParser.parseString(response.body()).getAsJsonObject();
                });

        CompletableFuture<String[]> locationFuture = reverseGeocode(lat, lon);

        return CompletableFuture.allOf(weatherFuture, locationFuture).thenApply(v -> {
            try {
                JsonObject weatherJson = weatherFuture.join();
                String[] locationData = locationFuture.join();

                JsonObject current = weatherJson.getAsJsonObject("current");
                double temp = current.get("temperature_2m").getAsDouble();
                int code = current.get("weather_code").getAsInt();
                int isDay = current.get("is_day").getAsInt();
                double windSpeed = current.has("windspeed_10m") ? current.get("windspeed_10m").getAsDouble() : 0.0;
                int humidity = current.has("relativehumidity_2m") ? current.get("relativehumidity_2m").getAsInt() : 0;

                String desc = decodeWeatherCode(code);
                String iconUrl = getIconUrl(code, isDay == 1);

                java.util.List<WeatherData.DailyForecast> dailyForecasts = new java.util.ArrayList<>();
                if (weatherJson.has("daily")) {
                    JsonObject daily = weatherJson.getAsJsonObject("daily");
                    var dates = daily.getAsJsonArray("time");
                    var maxTemps = daily.getAsJsonArray("temperature_2m_max");
                    var minTemps = daily.getAsJsonArray("temperature_2m_min");
                    var codes = daily.getAsJsonArray("weather_code");
                    var probs = daily.getAsJsonArray("precipitation_probability_max");

                    java.time.format.DateTimeFormatter inputFmt = java.time.format.DateTimeFormatter
                            .ofPattern("yyyy-MM-dd");
                    java.time.format.DateTimeFormatter outputFmt = java.time.format.DateTimeFormatter
                            .ofPattern("EEE d");

                    for (int i = 0; i < dates.size(); i++) {
                        String dateStr = dates.get(i).getAsString();
                        String dayName;
                        try {
                            java.time.LocalDate date = java.time.LocalDate.parse(dateStr, inputFmt);
                            dayName = date.format(outputFmt);
                            if (i == 0)
                                dayName = "Today";
                        } catch (Exception e) {
                            dayName = dateStr;
                        }

                        double max = maxTemps.get(i).getAsDouble();
                        double min = minTemps.get(i).getAsDouble();
                        int wCode = codes.get(i).getAsInt();
                        int prob = probs.get(i).getAsInt();
                        String dIcon = getIconUrl(wCode, true);

                        dailyForecasts.add(new WeatherData.DailyForecast(dayName, max, min, dIcon, prob));
                    }
                }

                String displayCity = locationData[0] != null ? locationData[0] : "Unknown Location";
                String displayArea = locationData[1] != null ? locationData[1]
                        : (locationData[2] != null ? locationData[2] : "");
                String pinCode = "N/A";

                return new WeatherData(temp, desc, iconUrl, displayCity, displayArea, pinCode, windSpeed, humidity,
                        dailyForecasts);
            } catch (Exception e) {
                return new WeatherData(0.0, "Error", "", "Error", "", "", 0.0, 0, new java.util.ArrayList<>());
            }
        });
    }

    private CompletableFuture<String[]> reverseGeocode(double lat, double lon) {
        String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&zoom=10&addressdetails=1",
                lat, lon);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "SmartTripPlanner/1.0")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200)
                        return new String[3];

                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    String[] result = new String[3];

                    if (json.has("address")) {
                        JsonObject address = json.getAsJsonObject("address");

                        if (address.has("city"))
                            result[0] = address.get("city").getAsString();
                        else if (address.has("town"))
                            result[0] = address.get("town").getAsString();
                        else if (address.has("village"))
                            result[0] = address.get("village").getAsString();
                        else if (address.has("municipality"))
                            result[0] = address.get("municipality").getAsString();

                        if (address.has("state"))
                            result[1] = address.get("state").getAsString();

                        if (address.has("country"))
                            result[2] = address.get("country").getAsString();
                    }
                    return result;
                });
    }

    private String decodeWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 71, 73, 75 -> "Snow";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown";
        };
    }

    private String getIconUrl(int code, boolean isDay) {
        String time = isDay ? "day" : "night";
        String icon = switch (code) {
            case 0 -> "01";
            case 1 -> "02";
            case 2 -> "03";
            case 3 -> "04";
            case 45, 48 -> "50";
            case 51, 53, 55 -> "09";
            case 61, 63, 65 -> "10";
            case 71, 73, 75 -> "13";
            case 95, 96, 99 -> "11";
            default -> "03";
        };
        return "http://openweathermap.org/img/wn/" + icon + time.substring(0, 1) + "@2x.png";
    }
}
