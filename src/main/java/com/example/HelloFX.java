package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));
        Scene scene = new Scene(loader.load(), 640, 480);

        HelloController ctrl = loader.getController();
        ctrl.setPrimaryStage(stage);

        stage.setTitle("NTFY Chat");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}
