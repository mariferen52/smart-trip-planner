package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.CurrencyData;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExchangeRateAPI implements ICurrencyService {
    private static final String API_KEY = "YOUR_API_KEY";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public CompletableFuture<CurrencyData> getExchangeRate(String fromCurrency, String toCurrency) {
        if (API_KEY.equals("YOUR_API_KEY")) {

            double mockRate = getMockExchangeRate(fromCurrency, toCurrency);
            String lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return CompletableFuture.completedFuture(
                    new CurrencyData(fromCurrency, toCurrency, mockRate, lastUpdated));
        }

        String url = String.format(
                "https://v6.exchangerate-api.com/v6/%s/pair/%s/%s",
                API_KEY, fromCurrency, toCurrency);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    double rate = json.get("conversion_rate").getAsDouble();
                    String timeLastUpdate = json.has("time_last_update_utc")
                            ? json.get("time_last_update_utc").getAsString()
                            : LocalDateTime.now().toString();
                    return new CurrencyData(fromCurrency, toCurrency, rate, timeLastUpdate);
                });
    }

    private double getMockExchangeRate(String from, String to) {

        if (from.equals("EUR") && to.equals("USD"))
            return 1.08;
        if (from.equals("USD") && to.equals("EUR"))
            return 0.93;
        if (from.equals("EUR") && to.equals("GBP"))
            return 0.86;
        if (from.equals("GBP") && to.equals("EUR"))
            return 1.16;
        if (from.equals("USD") && to.equals("GBP"))
            return 0.79;
        if (from.equals("GBP") && to.equals("USD"))
            return 1.27;
        if (from.equals("EUR") && to.equals("JPY"))
            return 162.50;
        if (from.equals("USD") && to.equals("JPY"))
            return 150.25;
        if (from.equals("EUR") && to.equals("INR"))
            return 90.50;
        if (from.equals("USD") && to.equals("INR"))
            return 83.75;
        return 1.0;
    }
}
