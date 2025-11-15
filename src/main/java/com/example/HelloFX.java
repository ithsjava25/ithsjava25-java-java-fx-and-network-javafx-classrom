package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloFX extends Application {

    /**
     * Main entry point for the JavaFX application
     * @param stage the primary stage for this application, onto which
     *              the application scene can be set
     * @throws Exception if loading the FXML file or initializing the scene fails
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));
        StackPane root = loader.load();

        MatrixRain rain = new MatrixRain(700, 600);
        rain.setOpacity(0.15);
        root.getChildren().add(0, rain);

        HelloController ctrl = loader.getController();
        ctrl.setPrimaryStage(stage);
        Scene scene = new Scene(root, 700, 600);
        stage.setTitle("Matrix Binary Chat");
        stage.setScene(scene);
        stage.show();
        rain.startAnimation();
    }

    /**
     * Application main method
     * @param args the command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch();
    }
}