package com.example;

import com.example.util.EnvLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloFX extends Application {

    /**
     * Initializes the primary application window from the ChatView.fxml layout and displays it.
     *
     * @param stage the primary stage to initialize and show
     * @throws IOException if the FXML resource cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("ChatView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Java25 Chat App");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Application entry point that loads environment configuration and launches the JavaFX application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        EnvLoader.load();
        launch();
    }
}