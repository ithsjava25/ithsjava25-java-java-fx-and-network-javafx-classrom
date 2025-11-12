package com.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    @FXML
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        System.out.println("Controller init: kopplar ListView");
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }
        messageView.setItems(model.getMessages());
    }

    @FXML
    private javafx.scene.control.TextField messageInput;


    public void sendMessage(ActionEvent actionEvent) {
        String content = messageInput.getText();
        model.setMessageToSend(content);
        model.sendMessage();
    }

    @FXML
    private void sendFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en fil att skicka");
        File selectedFile = fileChooser.showOpenDialog(messageInput.getScene().getWindow());

        if (selectedFile != null) {
            Path filePath = selectedFile.toPath();
            model.sendFile(filePath).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        System.out.println("Fil skickad: " + filePath.getFileName());
                    } else {
                        System.out.println("Misslyckades att skicka filen.");
                    }
                });
            });
        } else {
            System.out.println("Ingen fil vald.");
        }
    }

}

