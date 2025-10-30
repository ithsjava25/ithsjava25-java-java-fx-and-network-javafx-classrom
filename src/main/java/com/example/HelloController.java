package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private VBox chatBox;

    @FXML
    private void initialize() {

        messageInput.setOnAction((event) -> sendMessage());
        sendButton.setOnAction(event -> sendMessage());

    }
//StringProperty - HelloModel
    private void sendMessage() {
        String message = messageInput.getText();
        if (message != null) {
            Label messageLabel = new Label(message);
            chatBox.getChildren().add(messageLabel);
            messageInput.clear();
            //messageInput.replaceSelection("Skriv ett nytt meddelande");
        }
    }
}
