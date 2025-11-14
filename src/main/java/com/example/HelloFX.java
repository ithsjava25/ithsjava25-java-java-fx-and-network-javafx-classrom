package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelloFX extends Application {

    private NtfyConnection connection;
    private ImageServer imageServer;

    @Override
    public void start(Stage stage) {

        Dotenv dotenv = Dotenv.load();
        String hostName = dotenv.get("HOST_NAME");
        if (hostName == null || hostName.isBlank()) {
            showError("Configuration error", "HOST_NAME is not set in .env");
            return;
        }

        connection = new NtfyConnectionImpl(hostName);

        try {
            Path imageDir = Path.of("images");
            Files.createDirectories(imageDir);
            imageServer = new ImageServer(8081, imageDir);
        } catch (IOException e) {
            showError("Image server error", "Could not start local image server:\n" + e.getMessage());
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 600, 400);
        } catch (IOException e) {
            showError("FXML loading error", "Could not load GUI:\n" + e.getMessage());
            return;
        }

        HelloController controller = fxmlLoader.getController();
        controller.setConnection(connection);

        stage.setTitle("HelloFX Chat");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            System.out.println("🛑 Application closing...");

            try {
                if (connection != null) connection.stopReceiving();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (imageServer != null) imageServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showError(String title, String message) {
        System.err.println("❌ " + title + ": " + message);
    }

    public static void main(String[] args) {
        launch();
    }
}
