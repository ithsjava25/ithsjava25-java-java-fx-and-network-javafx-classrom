package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));
        StackPane root = loader.load();

        // Add MatrixRain behind the chat with very low opacity
        MatrixRain rain = new MatrixRain(700, 600);
        rain.setOpacity(0.15); // Very subtle so it doesn't interfere with chat readability
        root.getChildren().add(0, rain);

        HelloController ctrl = loader.getController();
        ctrl.setPrimaryStage(stage);
        Scene scene = new Scene(root, 700, 600);
        stage.setTitle("Matrix Binary Chat");
        stage.setScene(scene);
        stage.show();
        rain.startAnimation();
    }

    public static void main(String[] args) {
        launch();
    }
}