package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.UserProfile;
import com.isep.smarttripplanner.repository.UserProfileRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class SettingsController {
    @FXML
    private TextField usernameField;
    @FXML
    private ComboBox<String> currencyComboBox;

    private final UserProfileRepository profileRepository = new UserProfileRepository();
    private UserProfile currentProfile;

    @FXML
    public void initialize() {
        currencyComboBox.getItems().addAll("USD", "EUR", "GBP", "JPY", "CAD");
        loadSettings();
    }

    private void loadSettings() {
        currentProfile = profileRepository.getProfile();
        usernameField.setText(currentProfile.getUsername());
        currencyComboBox.setValue(currentProfile.getDefaultCurrency());
    }

    @FXML
    private void handleSaveSettings() {
        currentProfile.setUsername(usernameField.getText());
        currentProfile.setDefaultCurrency(currencyComboBox.getValue());
        profileRepository.saveProfile(currentProfile);
        // Show confirmation if needed
    }
}
