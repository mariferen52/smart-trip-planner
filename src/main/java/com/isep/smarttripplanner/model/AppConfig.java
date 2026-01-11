package com.isep.smarttripplanner.model;

public class AppConfig {
    private String defaultCurrency = "USD";

    private String targetCurrency;
    private java.time.LocalDate lastSummaryDate;

    public AppConfig() {
    }

    public AppConfig(String defaultCurrency, String targetCurrency) {
        this.defaultCurrency = defaultCurrency;
        this.targetCurrency = targetCurrency;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public java.time.LocalDate getLastSummaryDate() {
        return lastSummaryDate;
    }

    public void setLastSummaryDate(java.time.LocalDate lastSummaryDate) {
        this.lastSummaryDate = lastSummaryDate;
    }
}
