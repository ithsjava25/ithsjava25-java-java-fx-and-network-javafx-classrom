package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.IOException;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private TextArea myTextArea;

    @FXML
    private TextArea chatArea;

    @FXML
    private Button chatButton;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }
       messageView.setItems(model.getMessages());
       messageView.setCellFactory(showOnlyMessages());

    }

    private static Callback<ListView<NtfyMessageDto>, ListCell<NtfyMessageDto>> showOnlyMessages() {
        return List -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.message());
                }
            }
        };
    }

    public void sendMessage(ActionEvent actionEvent) {
        String input = chatArea.getText().trim();
        if (!input.isEmpty()) {
            model.setMessageToSend(input);
            model.sendMessage();
            chatArea.clear();
        }
    }
}

