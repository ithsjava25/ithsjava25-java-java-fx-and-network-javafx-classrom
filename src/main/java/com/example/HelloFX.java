package com.example;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the FXML file (absolute path is safer)
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("/com/example/hello-view.fxml"));
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hello-view2.fxml"));
        stage.setTitle("JavaFX Chat (ntfy)");
        Parent root = fxmlLoader.load();

        // Create the scene
        Scene scene = new Scene(root, 640, 480);

        // Add the stylesheet (optional)
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/application.css"),
                        "Missing resource: application.css"
                ).toExternalForm()
        );

        // Show the window
        stage.setTitle("Hello MVC");

        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            event.consume();
            logout(stage);

        });
    }

    private void sendFileToBackend(File file) {
        try {
            String backendUrl = System.getenv("BACKEND_URL");
            if (backendUrl == null) {
                System.err.println("BACKEND_URL not set");
                return;
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(backendUrl).openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Title", file.getName());

            try (OutputStream os = conn.getOutputStream();
                 FileInputStream fis = new FileInputStream(file)) {
                fis.transferTo(os);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Upload response: " + responseCode);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void logout(Stage stage) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You are about to log out");
        alert.setContentText("Do you want to save before exiting?");

        if(alert.showAndWait().get() == ButtonType.OK){
            //stage = (Stage) scenePane.getScene().getWindow();
            System.out.println("You successfully logged out");
            stage.close();
        }
    }


    public static void main(String[] args) {
        launch();
    }

}