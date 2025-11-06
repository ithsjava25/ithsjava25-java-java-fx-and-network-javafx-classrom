package com.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    private Label messageLabel;

    @FXML
    private ListView<NtfyMessageDto> messageView;

    @FXML
    private TextArea messageInput;

    // Formatter för tid
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @FXML
    private void initialize() {
        // Visa hälsning
        messageLabel.setText(model.getGreeting());

        // Koppla ListView till modellens meddelanden
        messageView.setItems(model.getMessages());

        // Bind TextArea till modellens property
        messageInput.textProperty().bindBidirectional(model.messageToSendProperty());

        // Snyggare visning av meddelanden
        messageView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    setText("[" + timeFormatter.format(Instant.ofEpochMilli(msg.time())) + "] "
                            + msg.message());
                }
            }
        });

        // Scrolla automatiskt ner till senaste meddelandet
        model.getMessages().addListener((javafx.collections.ListChangeListener<NtfyMessageDto>) change -> {
            Platform.runLater(() -> {
                if (!messageView.getItems().isEmpty()) {
                    messageView.scrollTo(messageView.getItems().size() - 1);
                }
            });
        });
    }

    @FXML
    private void sendMessage(ActionEvent actionEvent) {
        model.sendMessage();
        messageInput.clear();
    }
}
