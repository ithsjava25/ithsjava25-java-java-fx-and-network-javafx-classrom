package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

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
}

