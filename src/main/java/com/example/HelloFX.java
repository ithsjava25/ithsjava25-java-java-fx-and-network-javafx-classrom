package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        String hostName = dotenv.get("HOST_NAME");
        if (hostName != null) {
            System.out.println("Connected to: " + hostName);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 640, 480);
        stage.setTitle("Hello MVC");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}