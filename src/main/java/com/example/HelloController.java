package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    @FXML private TextArea chatArea;
    @FXML private TextField inputField;
    @FXML private Button sendButton;

    private final HelloModel model = new HelloModel();

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        model.startListening(message -> Platform.runLater(() -> {
            chatArea.appendText(message + "\n");
        }));
        sendButton.setOnAction(e->sendMessage());
    }
    @FXML
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        try {
            chatArea.appendText("Du: " + msg + "\n");

            model.sendMessage(msg);
            inputField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
