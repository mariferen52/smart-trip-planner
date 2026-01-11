package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.AppConfig;
import com.isep.smarttripplanner.repository.AppConfigRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class SettingsController {
    @FXML
    private ComboBox<String> currencyComboBox;

    @FXML
    private ComboBox<String> targetCurrencyComboBox;

    private final AppConfigRepository configRepository = new AppConfigRepository();
    private AppConfig currentConfig;

    @FXML
    public void initialize() {
        String[] currencies = {
                "USD", "EUR", "TRY", "GEL", "INR"
        };
        currencyComboBox.getItems().addAll(currencies);
        targetCurrencyComboBox.getItems().addAll(currencies);
        loadSettings();
    }

    private void loadSettings() {
        currentConfig = configRepository.getConfig();
        currencyComboBox.setValue(currentConfig.getDefaultCurrency());
        targetCurrencyComboBox.setValue(currentConfig.getTargetCurrency());
    }

    @FXML
    private void handleSaveSettings() {
        currentConfig.setDefaultCurrency(currencyComboBox.getValue());
        currentConfig.setTargetCurrency(targetCurrencyComboBox.getValue());
        configRepository.saveConfig(currentConfig);

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Settings saved successfully!");
        alert.showAndWait();
    }
}
