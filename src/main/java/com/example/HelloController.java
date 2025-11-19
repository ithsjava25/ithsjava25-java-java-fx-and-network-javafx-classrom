package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

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

        if (messageInput != null) {
            messageInput.textProperty().bindBidirectional(model.messageToSendProperty());
        }

        messageView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    var time = java.time.Instant.ofEpochSecond(item.time())
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalTime();
                    var formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                    setText(time.format(formatter) + " " + item.message());
                }
            }
        });
    }


    public void sendMessage(ActionEvent actionEvent) {
        model.sendMessage();
    }
}
