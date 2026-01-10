package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.CurrencyData;
import java.util.concurrent.CompletableFuture;

public interface ICurrencyService {
    CompletableFuture<CurrencyData> getExchangeRate(String fromCurrency, String toCurrency);
}
