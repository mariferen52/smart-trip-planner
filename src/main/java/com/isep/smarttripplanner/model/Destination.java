package com.isep.smarttripplanner.model;

import java.time.LocalDate;

public class Destination {
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private LocalDate destinationStartDate;
    private LocalDate destinationEndDate;

    public Destination() {
    }

    public Destination(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setDestinationEndDate(LocalDate destinationEndDate) {
        this.destinationEndDate = destinationEndDate;
    }

    public void setDestinationStartDate(LocalDate destinationStartDate) {
        this.destinationStartDate = destinationStartDate;
    }

    public LocalDate getDestinationEndDate() {
        return destinationEndDate;
    }

    public LocalDate getDestinationStartDate() {
        return destinationStartDate;
    }

}
