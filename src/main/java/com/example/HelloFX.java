package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Globalt fel fångat: " + throwable.getMessage());
        });
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();

        // Sätt primaryStage i controllern
        HelloController controller = fxmlLoader.getController();
        controller.setPrimaryStage(stage);

        Scene scene = new Scene(root, 640, 480);
        stage.setTitle("Hello MVC with File Support");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}