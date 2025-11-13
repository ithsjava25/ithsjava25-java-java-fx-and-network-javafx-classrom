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
import javafx.stage.FileChooser;

import java.io.File;

public class ChatController {

    @FXML
    private ListView<String> messageListView;

    @FXML
    private TextField messageInput;

    @FXML
    private Label statusLabel;

    private ChatModel model;

    /**
     * Initializes controller state and configures the message list view and its listeners.
     *
     * Instantiates the ChatModel, updates the status display to online, installs a custom cell
     * factory to control presentation of message strings, and registers a listener on the model's
     * message list that appends newly received NtfyMessage text (prefixed with an emoji) to the
     * ListView and scrolls to the newest item.
     */
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

    /**
     * Sends the current text from the message input through the model.
     *
     * If the input contains non-empty text, initiates a background send operation.
     * On success the input is cleared and the status is updated to online.
     * On failure an error message is written to standard error and the status is updated to offline.
     */
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

    /**
     * Opens a file chooser for the user to pick a file and sends the selected file via the model.
     *
     * If the user selects a file, the file is sent on a background thread; on success a confirmation
     * message is printed to standard output and on failure an error message is printed to standard error.
     * If the user cancels the dialog, no action is taken.
     */
    @FXML
    private void handleAttachFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        File file = fileChooser.showOpenDialog(messageInput.getScene().getWindow());

        if (file != null) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    model.sendFile(file);
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                System.out.println("✅ File info sent: " + file.getName());
            });

            task.setOnFailed(e -> {
                System.err.println("❌ File send failed: " + task.getException().getMessage());
            });

            new Thread(task).start();
        }
    }

    /**
     * Updates the status label to show an "online" state.
     *
     * If a status label is present, sets its text to "● online" and applies a red text color,
     * 10px font size, and a subtle drop shadow; does nothing if the label is null.
     */
    private void updateStatusOnline() {
        if (statusLabel != null) {
            statusLabel.setText("● online");
            statusLabel.setStyle("-fx-text-fill: #c93939; " +
                    "-fx-font-size: 10px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        }
    }

    /**
     * Update the status label to indicate the application is offline and apply muted styling.
     *
     * If the status label exists, sets its text to "● offline" and applies a muted brownish color,
     * a small font size, and a subtle drop shadow.
     */
    private void updateStatusOffline() {
        if (statusLabel != null) {
            statusLabel.setText("● offline");
            statusLabel.setStyle("-fx-text-fill: #6b5d54; " +
                    "-fx-font-size: 10px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        }
    }
}