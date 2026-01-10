package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.model.TripStatus;
import com.isep.smarttripplanner.model.UserProfile;
import com.isep.smarttripplanner.repository.TripRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class ProfileController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label totalTripsLabel;

    @FXML
    private Label completedTripsLabel;

    @FXML
    private Label plannedTripsLabel;

    @FXML
    private Label totalBudgetLabel;

    @FXML
    private Label destinationsLabel;

    @FXML
    public void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        UserProfile profile = new UserProfile("Traveler", "traveler@smarttrip.com");
        
        // Load stats from database
        try {
            TripRepository repo = new TripRepository();
            List<Trip> allTrips = repo.findAllTrips();
            
            int totalTrips = allTrips.size();
            int completedTrips = 0;
            int plannedTrips = 0;
            double totalBudget = 0;
            int totalDestinations = 0;
            
            for (Trip trip : allTrips) {
                if (trip.getStatus() == TripStatus.COMPLETED) {
                    completedTrips++;
                    totalBudget += trip.getBudget();
                    if (trip.getDestinations() != null) {
                        totalDestinations += trip.getDestinations().size();
                    }
                } else if (trip.getStatus() == TripStatus.PLANNED || trip.getStatus() == TripStatus.ONGOING) {
                    plannedTrips++;
                }
            }
            
            profile.setTotalTrips(totalTrips);
            profile.setCompletedTrips(completedTrips);
            profile.setPlannedTrips(plannedTrips);
            profile.setTotalBudgetSpent(totalBudget);
            profile.setTotalDestinationsVisited(totalDestinations);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Update UI
        if (usernameLabel != null) usernameLabel.setText(profile.getUsername());
        if (emailLabel != null) emailLabel.setText(profile.getEmail());
        if (totalTripsLabel != null) totalTripsLabel.setText(String.valueOf(profile.getTotalTrips()));
        if (completedTripsLabel != null) completedTripsLabel.setText(String.valueOf(profile.getCompletedTrips()));
        if (plannedTripsLabel != null) plannedTripsLabel.setText(String.valueOf(profile.getPlannedTrips()));
        if (totalBudgetLabel != null) totalBudgetLabel.setText(String.format("$%.2f", profile.getTotalBudgetSpent()));
        if (destinationsLabel != null) destinationsLabel.setText(String.valueOf(profile.getTotalDestinationsVisited()));
    }
}
