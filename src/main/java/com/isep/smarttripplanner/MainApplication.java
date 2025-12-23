package com.isep.smarttripplanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        primaryStage.setTitle("Smart Trip Planner");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.setFullScreen(false);
        initRootLayout();

    }

    private void initRootLayout() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("root-view.fxml"));
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth() * 0.7;
        double screenHeight = screenWidth * (9.0 / 16.0);
        Scene scene = new Scene(fxmlLoader.load(), screenWidth, screenHeight);
        RootController controller = fxmlLoader.getController();
        controller.setPrimaryStage(primaryStage);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}