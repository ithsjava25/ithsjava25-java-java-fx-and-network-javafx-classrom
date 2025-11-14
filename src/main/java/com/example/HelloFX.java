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

        // Starta ImageServer på separat daemon-tråd
        Thread serverThread = new Thread(() -> {
            try {
                imageServer = new ImageServer(8081);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Ladda FXML
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        // Hämta controller och injicera connection
        HelloController controller = fxmlLoader.getController();
        controller.setConnection(connection);

        stage.setTitle("HelloFX Chat");
        stage.setScene(scene);
        stage.show();

        // Säkerställ att connection och server stoppas vid stängning
        stage.setOnCloseRequest(event -> {
            System.out.println("🛑 Application closing...");

            try {
                if (connection != null) {
                    connection.stopReceiving();
                    System.out.println("🔌 NtfyConnection stopped");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (imageServer != null) {
                    imageServer.stop();
                    System.out.println("🛑 Image server stopped");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Ingen need att joina daemon-tråden; den avslutas automatiskt
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
