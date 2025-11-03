package com.example.controller;

import com.example.model.ChatModel;
import com.example.model.NtfyMessage;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.concurrent.Task;
import javafx.geometry.Pos;

public class ChatController {

    @FXML
    private ListView<String> messageListView;

    @FXML
    private TextField messageInput;

    @FXML
    private Label statusLabel;

    private ChatModel model;

    @FXML
    public void initialize() {
        this.model = new ChatModel();

        updateStatusOnline();

        messageListView.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER_LEFT);
                    setStyle("-fx-background-color: rgba(255, 250, 240, 0.6); " +
                            "-fx-text-fill: #3d3d3d; " +
                            "-fx-padding: 14 18 14 18; " +
                            "-fx-border-color: rgba(214, 69, 69, 0.15); " +
                            "-fx-border-width: 0 0 0 3; " +
                            "-fx-font-size: 13px; " +
                            "-fx-background-radius: 0; " +
                            "-fx-border-radius: 0; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 2);");
                }
            }
        });

        model.getMessages().addListener((javafx.collections.ListChangeListener.Change<? extends NtfyMessage> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (NtfyMessage msg : change.getAddedSubList()) {
                        String formatted = "🌸 " + msg.message();
                        messageListView.getItems().add(formatted);
                    }
                    messageListView.scrollTo(messageListView.getItems().size() - 1);
                }
            }
        });
    }

    @FXML
    private void handleSendButtonAction() {
        String text = messageInput.getText();
        if (text != null && !text.trim().isEmpty()) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    model.sendMessage(text.trim());
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                messageInput.clear();
                updateStatusOnline();
            });

            task.setOnFailed(e -> {
                System.err.println("Send failed: " + task.getException().getMessage());
                updateStatusOffline();
            });

            new Thread(task).start();
        }
    }

    private void updateStatusOnline() {
        if (statusLabel != null) {
            statusLabel.setText("● online");
            statusLabel.setStyle("-fx-text-fill: #c93939; " +
                    "-fx-font-size: 10px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        }
    }

    private void updateStatusOffline() {
        if (statusLabel != null) {
            statusLabel.setText("● offline");
            statusLabel.setStyle("-fx-text-fill: #6b5d54; " +
                    "-fx-font-size: 10px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        }
    }
}