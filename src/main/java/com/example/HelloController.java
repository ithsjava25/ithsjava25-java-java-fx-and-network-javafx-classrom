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

    private final HelloModel model = new HelloModel();

    @FXML
    public ListView<NtfyMessageDto> messageView ;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField messageField;

    @FXML
    private void initialize() {
        messageLabel.setText(model.getGreeting());
        messageView.setItems(model.getMessages());
    }

    public void sendMessage(ActionEvent actionEvent) {
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            model.sendMessage(text);
            messageField.clear();
        }
    }
}
