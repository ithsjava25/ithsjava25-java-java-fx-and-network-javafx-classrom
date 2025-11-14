package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        NtfyConnection ntfyService = new NtfyConnectionImpl();
        ChatModel model = new ChatModel(ntfyService);

        model.startReceiving();

        FXMLLoader fxmlLoader = new FXMLLoader(
                ChatApplication.class.getResource("chat-view.fxml"));

        ChatController controller = new ChatController(model);
        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("JavaFX Ntfy Chat App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}