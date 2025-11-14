package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloFX extends Application {

    private NtfyConnection connection;
    private ImageServer imageServer;

    @Override
    public void start(Stage stage) throws IOException {
        // Skapa en enda NtfyConnection-instans
        connection = new NtfyConnectionImpl();

        // Starta ImageServer på separat tråd och spara referensen
        new Thread(() -> {
            try {
                imageServer = new ImageServer(8081);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Ladda FXML
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        // Hämta controller och injicera model
        HelloController controller = fxmlLoader.getController();
        HelloModel model = new HelloModel(connection);
        controller.setModel(model);

        stage.setTitle("HelloFX Chat");
        stage.setScene(scene);
        stage.show();

        // Stäng trådar och server vid stängning
        stage.setOnCloseRequest(event -> {
            if (connection != null) connection.stopReceiving();
            if (imageServer != null) imageServer.stop();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
