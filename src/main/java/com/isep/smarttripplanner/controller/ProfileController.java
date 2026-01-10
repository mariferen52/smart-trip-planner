package com.isep.smarttripplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    public void initialize() {
        if (usernameLabel != null) {
            usernameLabel.setText("User");
        }
        if (emailLabel != null) {
            emailLabel.setText("user@example.com");
        }
    }
}
