package com.isep.smarttripplanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth() * 0.7;
        double screenHeight = screenWidth * (9.0/16.0);

        Scene scene = new Scene(fxmlLoader.load(), screenWidth, screenHeight);
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setTitle("Main Page");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.setFullScreen(false);
        stage.show();
    }
}