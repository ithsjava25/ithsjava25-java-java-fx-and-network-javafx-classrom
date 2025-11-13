package com.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML private Label messageLabel;
    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private TextArea messageInput;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    @FXML
    private void initialize() {
        //visa välkomstmeddelande
        messageLabel.setText(model.getGreeting());

        messageView.setItems(model.getMessages());

        messageInput.textProperty().bindBidirectional(model.messageToSendProperty());

        //formatera meddelanden i ListView
        messageView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    setText("[" + timeFormatter.format(Instant.ofEpochMilli(msg.time())) + "] " + msg.message());
                }
            }
        });

        // Scrolla automatiskt till senaste meddelandet
        model.getMessages().addListener((javafx.collections.ListChangeListener<NtfyMessageDto>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    Platform.runLater(() -> {
                        int size = messageView.getItems().size();
                        if (size > 0) {
                            messageView.scrollTo(size - 1);
                        }
                    });
                }
            }
        });
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        //skicka asynkront – HelloModel hanterar rensning och callback
        model.sendMessageAsync(success -> {
            if (!success) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Kunde inte skicka meddelandet.");
                    alert.show();
                });
            }
        });
    }
}