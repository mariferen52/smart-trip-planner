package com.isep.smarttripplanner.model;

public class WeatherData {
    private double temperature;
    private String description;
    private String iconUrl;
    private String cityName;
    private String area;
    private String pinCode;

    public WeatherData(double temperature, String description, String iconUrl, String cityName, String area,
            String pinCode) {
        this.temperature = temperature;
        this.description = description;
        this.iconUrl = iconUrl;
        this.cityName = cityName;
        this.area = area;
        this.pinCode = pinCode;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }
}
