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

        // Starta ImageServer på separat tråd
        Thread serverThread = new Thread(() -> {
            try {
                imageServer = new ImageServer(8081);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true); // avslutas automatiskt vid app-stopp
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

        // Säkerställ stängning av server och connection vid fönsterstängning
        stage.setOnCloseRequest(event -> {
            System.out.println("🛑 Application closing...");

            if (connection != null) {
                connection.stopReceiving();
                System.out.println("🔌 NtfyConnection stopped");
            }

            if (imageServer != null) {
                imageServer.stop();
            }

            // Vänta på server-trådens avslut (valfritt)
            try {
                serverThread.join(500); // max 0.5 sekunder
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
