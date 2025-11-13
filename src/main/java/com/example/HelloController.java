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
    private TextField inputField;

    @FXML
    private TextField topicField;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }

        inputField.textProperty().bindBidirectional(model.messageToSendProperty());

        topicField.textProperty().bindBidirectional(model.topicProperty());

        messageView.setItems(model.getMessages());

        model.connectToTopic();
    }

    public void sendMessage(ActionEvent actionEvent) {
        if (!inputField.getText().trim().isEmpty()) {
            model.sendMessage();
            inputField.clear();
        }
    }

    public void connectToTopic(ActionEvent actionEvent) {
        model.connectToTopic();
    }
}