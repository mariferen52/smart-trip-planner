package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.WeatherData;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OpenWeatherMap implements IWeatherService {
    private static final String API_KEY = "YOUR_API_KEY";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public CompletableFuture<WeatherData> getForecast(double lat, double lon) {
        if (API_KEY.equals("YOUR_API_KEY")) {
            // Demo Mode: Use reverse geocoding to get actual city name
            return reverseGeocode(lat, lon).thenApply(locationData -> {
                String cityName = locationData[0];
                String area = locationData[1];
                String country = locationData[2];
                
                String displayCity = cityName != null ? cityName : "Unknown Location";
                String displayArea = area != null ? area : (country != null ? country : "");
                String mockPinCode = String.format("%05d", (Math.abs((int) (lat * 100 + lon * 100)) % 90000 + 10000));
                
                return new WeatherData(22.0, "Partly Cloudy",
                        "http://openweathermap.org/img/wn/02d@2x.png", displayCity, displayArea, mockPinCode);
            }).exceptionally(ex -> {
                // Fallback if geocoding fails
                return new WeatherData(22.0, "Partly Cloudy",
                        "http://openweathermap.org/img/wn/02d@2x.png", "Unknown City", "", "00000");
            });
        }

        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric", lat, lon,
                API_KEY);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    double temp = json.getAsJsonObject("main").get("temp").getAsDouble();
                    String desc = json.getAsJsonArray("weather").get(0).getAsJsonObject().get("description")
                            .getAsString();
                    String icon = json.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString();
                    String cityName = json.has("name") ? json.get("name").getAsString() : "Unknown City";
                    String area = json.has("sys") && json.getAsJsonObject("sys").has("country")
                            ? json.getAsJsonObject("sys").get("country").getAsString()
                            : "General Area";
                    String pinCode = "N/A";
                    String iconUrl = "http://openweathermap.org/img/wn/" + icon + "@2x.png";
                    return new WeatherData(temp, desc, iconUrl, cityName, area, pinCode);
                });
    }

    private CompletableFuture<String[]> reverseGeocode(double lat, double lon) {
        // Use OpenStreetMap Nominatim for free reverse geocoding
        String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&zoom=10&addressdetails=1",
                lat, lon);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "SmartTripPlanner/1.0")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    String[] result = new String[3];
                    
                    if (json.has("address")) {
                        JsonObject address = json.getAsJsonObject("address");
                        
                        // Try to get city name from various fields
                        if (address.has("city")) {
                            result[0] = address.get("city").getAsString();
                        } else if (address.has("town")) {
                            result[0] = address.get("town").getAsString();
                        } else if (address.has("village")) {
                            result[0] = address.get("village").getAsString();
                        } else if (address.has("municipality")) {
                            result[0] = address.get("municipality").getAsString();
                        } else if (address.has("county")) {
                            result[0] = address.get("county").getAsString();
                        }
                        
                        // Get state/region
                        if (address.has("state")) {
                            result[1] = address.get("state").getAsString();
                        } else if (address.has("region")) {
                            result[1] = address.get("region").getAsString();
                        }
                        
                        // Get country
                        if (address.has("country")) {
                            result[2] = address.get("country").getAsString();
                        }
                    }
                    
                    return result;
                });
    }
}
