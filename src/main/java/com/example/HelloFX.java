package com.example;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

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