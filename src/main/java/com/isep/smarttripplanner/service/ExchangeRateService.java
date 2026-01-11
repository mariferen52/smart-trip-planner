package com.isep.smarttripplanner.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExchangeRateService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String API_URL = "https://open.er-api.com/v6/latest/";

    public CompletableFuture<Double> getExchangeRate(String baseCurrency, String targetCurrency) {
        String url = API_URL + baseCurrency;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "SmartTripPlanner/1.0")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Exchange Rate API Error: " + response.statusCode());
                    }
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    if (json.has("rates")) {
                        JsonObject rates = json.getAsJsonObject("rates");
                        if (rates.has(targetCurrency)) {
                            return rates.get(targetCurrency).getAsDouble();
                        }
                    }
                    throw new RuntimeException("Currency not found: " + targetCurrency);
                });
    }
}
