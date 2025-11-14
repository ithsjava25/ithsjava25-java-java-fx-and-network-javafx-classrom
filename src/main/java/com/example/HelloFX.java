package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Ladda FXML
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();

        // Skapa scenen
        Scene scene = new Scene(root, 640, 480);

        // Koppla in CSS-styling
        scene.getStylesheets().add(HelloFX.class.getResource("style.css").toExternalForm());

        // Sätt titel
        stage.setTitle("Java Chat");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
