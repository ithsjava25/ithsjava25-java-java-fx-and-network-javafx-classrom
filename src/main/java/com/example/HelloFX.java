package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HelloFX extends Application {
    private NtfyConnectionImpl connection;

    @Override
    public void start(Stage stage) throws Exception {
        connection = new NtfyConnectionImpl();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 640, 480);
        stage.setTitle("Hello MVC");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            System.out.println("🟡 Closing app, stopping Ntfy receiver...");
            if (connection != null) {
                connection.stopReceiving(); // 🛑 stoppar lyssnartråden
            }
        });

    }

    public static void main(String[] args) {
        launch();
    }

}