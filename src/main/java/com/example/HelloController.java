package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private Label messageLabel;
    @FXML
    private TextField messageInput;

    @FXML
    private void initialize() {
        messageLabel.setText(model.getGreeting());
        messageView.setItems(model.getMessages());
        messageInput.textProperty().bindBidirectional(model.messageToSendProperty());
    }

    public void sendMessage(ActionEvent actionEvent) {
        model.sendMessage();
        messageInput.clear();
    }
}