package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    private Label messageLabel;

    @FXML
    private ListView<NtfyMessageDto> messageView;

    @FXML
    private TextField inputMessage;

    @FXML
    private void initialize() {
        // Bind greeting label
        messageLabel.setText(model.getGreeting());

        // Bind ListView to model messages
        messageView.setItems(model.getMessages());
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        String text = inputMessage.getText();
        if (text != null && !text.isBlank()) {
            model.sendMessage(text);
            inputMessage.clear();
        }
    }

    @FXML
    private void attachFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(messageView.getScene().getWindow());
        if (file != null) {
            model.sendFile(file);
        }
    }
}

