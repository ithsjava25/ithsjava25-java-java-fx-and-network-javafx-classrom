package com.example;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML private TextField messageField;
    @FXML private VBox chatBox;
    @FXML private ScrollPane chatScroll;
    @FXML private Label statusLabel;

    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        model.sendMessage(message);
        messageField.clear();
    }

    @FXML
    private void handleSendImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en bild att skicka");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Bildfiler", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File imageFile = fileChooser.showOpenDialog(null);
        if (imageFile != null) {
            boolean success = model.sendImage(imageFile);
            if (success) {
                // GUI-uppdatering hanteras av modellen via imageUrl
            } else {
                showStatus("Misslyckades att skicka bilden: " + imageFile.getName());
            }
        }
    }

    @FXML
    private void initialize() {
        if (messageField != null) {
            messageField.setOnAction(e -> handleSend());
        }

        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (NtfyMessageDto msg : change.getAddedSubList()) {
                        boolean sentByUser = msg.topic().equals(HelloModel.DEFAULT_TOPIC);

                        if (msg.imageUrl() != null) {
                            addImageBubbleFromUrl(msg.imageUrl(), sentByUser);
                        } else if (msg.message() != null) {
                            addMessageBubble(msg.message(), sentByUser, msg.time());
                        }
                    }
                }
            }
        });

        chatBox.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1.0));
    }

    private void addMessageBubble(String text, boolean isSentByUser, long timestamp) {
        if (text == null || text.isBlank()) return;

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.getStyleClass().add(isSentByUser ? "sent-message" : "received-message");

        String timeString = Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalTime().toString().substring(0, 5);
        Label timeLabel = new Label(timeString);
        timeLabel.getStyleClass().add("timestamp");

        VBox bubble = new VBox(messageLabel, timeLabel);
        bubble.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox container = new HBox(bubble);
        container.setPadding(new Insets(5));
        container.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatBox.getChildren().add(container);
    }

    private void addImageBubbleFromUrl(String url, boolean isSentByUser) {
        ImageView imageView = new ImageView(new Image(url, true));
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        imageView.setOnMouseClicked(e -> {
            Stage stage = new Stage();
            ImageView bigView = new ImageView(new Image(url));
            bigView.setPreserveRatio(true);
            bigView.setFitWidth(800);

            ScrollPane sp = new ScrollPane(bigView);
            sp.setFitToWidth(true);

            stage.setScene(new Scene(sp, 900, 700));
            stage.setTitle("Bildvisning");
            stage.show();
        });

        String timeString = Instant.now()
                .atZone(ZoneId.systemDefault())
                .toLocalTime().toString().substring(0, 5);
        Label timeLabel = new Label(timeString);
        timeLabel.getStyleClass().add("timestamp");

        VBox box = new VBox(imageView, timeLabel);
        box.setSpacing(3);
        box.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox wrapper = new HBox(box);
        wrapper.setPadding(new Insets(5));
        wrapper.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatBox.getChildren().add(wrapper);
    }

    public void showStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}
