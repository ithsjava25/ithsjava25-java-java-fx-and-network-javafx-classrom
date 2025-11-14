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

        // Lägg till MatrixRain som bakgrund
        MatrixRain rain = new MatrixRain(700, 600);
        root.getChildren().add(0, rain);

        HelloController ctrl = loader.getController();
        ctrl.setPrimaryStage(stage);

        Scene scene = new Scene(root, 700, 600);
        stage.setTitle("NTFY Chat");
        stage.setScene(scene);
        stage.show();

        rain.startAnimation(); // starta matrix-effekten
    }

    public static void main(String[] args) {
        launch();
    }
}
