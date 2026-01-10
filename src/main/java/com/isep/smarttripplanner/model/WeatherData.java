package com.isep.smarttripplanner.model;

public class WeatherData {
    private final double temperature;
    private final String description;
    private final String iconUrl;
    private final String cityName;
    private final String area;
    private final String pinCode;
    private final double windSpeed;
    private final int humidity;
    private final java.util.List<DailyForecast> dailyForecasts;

    public WeatherData(double temperature, String description, String iconUrl, String cityName, String area,
            String pinCode, double windSpeed, int humidity, java.util.List<DailyForecast> dailyForecasts) {
        this.temperature = temperature;
        this.description = description;
        this.iconUrl = iconUrl;
        this.cityName = cityName;
        this.area = area;
        this.pinCode = pinCode;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.dailyForecasts = dailyForecasts;
    }

    // ... existing getters ...

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getCityName() {
        return cityName;
    }

    public String getArea() {
        return area;
    }

    public String getPinCode() {
        return pinCode;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }

    public java.util.List<DailyForecast> getDailyForecasts() {
        return dailyForecasts;
    }

    public static class DailyForecast {
        private final String day;
        private final double maxTemp;
        private final double minTemp;
        private final String iconUrl;
        private final int chanceOfRain;

        public DailyForecast(String day, double maxTemp, double minTemp, String iconUrl, int chanceOfRain) {
            this.day = day;
            this.maxTemp = maxTemp;
            this.minTemp = minTemp;
            this.iconUrl = iconUrl;
            this.chanceOfRain = chanceOfRain;
        }

        public String getDay() {
            return day;
        }

        public double getMaxTemp() {
            return maxTemp;
        }

        public double getMinTemp() {
            return minTemp;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public int getChanceOfRain() {
            return chanceOfRain;
        }
    }
}
