package com.isep.smarttripplanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;


public class MainController {

    private Stage stage;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private GridPane mainScene;

    @FXML
    private Region setScreenIcon;

    @FXML
    private Label close;


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize(){
        mainScene.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateFontSize(newVal.doubleValue());
        });
    }

    private void updateFontSize(double screenHeight){
        double fontSize = screenHeight / 40.0;
        close.setStyle("-fx-font-size: " + fontSize + "px; ");
        setScreenIcon.setStyle("-fx-min-height: " + fontSize + "px; " +
                       "-fx-max-height: " + fontSize + "px; " +
                       "-fx-min-width: " + fontSize + "px; " +
                       "-fx-max-width: " + fontSize + "px; ");
    }


    @FXML
    private void close() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void setScreen() {
        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
        }else{
            stage.setFullScreen(true);
        }
    }

    @FXML
    private void getScreenCoordinates(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void dragScreen(MouseEvent event) {
        if (!stage.isFullScreen()) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }
}
