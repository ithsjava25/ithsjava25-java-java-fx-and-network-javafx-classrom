package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

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
    private TextField messageInput;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }
        messageView.setItems(model.getMessages());


        model.messageToSendProperty().bindBidirectional(messageInput.textProperty());


        messageView.setCellFactory(lv -> new javafx.scene.control.ListCell<NtfyMessageDto>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.message() != null ? item.message() : "(No message content)");
                }
            }
        });
    }

    public void sendMessage(ActionEvent actionEvent) {
        model.sendMessage();

        messageInput.clear();
    }

    public void attachFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Attach");
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            model.sendFile(file);
        }
    }
}