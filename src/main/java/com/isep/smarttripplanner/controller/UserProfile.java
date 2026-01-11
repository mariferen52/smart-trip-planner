package com.isep.smarttripplanner.model;

public class UserProfile {
    private String username;
    private String email;
    private String defaultCurrency;
    private int totalTrips;
    private int completedTrips;
    private double totalBudgetSpent;
    private int totalDestinationsVisited;

    public UserProfile() {
    }

    public UserProfile(String username, String email) {
        this.username = username;
        this.email = email;
        this.defaultCurrency = "USD";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public int getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(int totalTrips) {
        this.totalTrips = totalTrips;
    }

    public int getCompletedTrips() {
        return completedTrips;
    }

    public void setCompletedTrips(int completedTrips) {
        this.completedTrips = completedTrips;
    }

    public double getTotalBudgetSpent() {
        return totalBudgetSpent;
    }

    public void setTotalBudgetSpent(double totalBudgetSpent) {
        this.totalBudgetSpent = totalBudgetSpent;
    }

    public int getTotalDestinationsVisited() {
        return totalDestinationsVisited;
    }

    public void setTotalDestinationsVisited(int totalDestinationsVisited) {
        this.totalDestinationsVisited = totalDestinationsVisited;
    }
}
