package com.isep.smarttripplanner.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Trip {
    private String id;
    private String title;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private double budget;
    private TripStatus status = TripStatus.PLANNED;
    private List<Destination> destinations;

    public Trip(){}

    public Trip(String title, LocalDate startDate, LocalDate endDate, double budget, List<Destination> destinations){
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.tripStartDate = startDate;
        this.tripEndDate = endDate;
        this.budget = budget;
        this.destinations = destinations;
    }

    public String getId() {
        return id;
    }

    public String getTitle(){
        return title;
    }

    public LocalDate getStartDate() {
        return tripStartDate;
    }

    public LocalDate getTripEndDate() {
        return tripEndDate;
    }

    public double getBudget() {
        return budget;
    }

    public TripStatus getStatus() {
        return status;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public void setTripEndDate(LocalDate tripEndDate) {
        this.tripEndDate = tripEndDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDate(LocalDate startDate) {
        this.tripStartDate = startDate;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }
}
