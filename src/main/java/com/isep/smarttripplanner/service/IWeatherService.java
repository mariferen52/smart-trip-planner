package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.WeatherData;
import java.util.concurrent.CompletableFuture;

public interface IWeatherService {
    CompletableFuture<WeatherData> getForecast(double lat, double lon);

    CompletableFuture<double[]> getCoordinates(String city);

    CompletableFuture<String> getCityName(double lat, double lon);
}
