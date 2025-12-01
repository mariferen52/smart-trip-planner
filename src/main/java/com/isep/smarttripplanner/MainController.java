package com.isep.smarttripplanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void close() {
        Platform.exit();
        System.exit(0);
    }
}
