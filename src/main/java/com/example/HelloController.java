package com.example;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private Label statusLabel;
    @FXML private TextField messageField;
    @FXML private VBox chatBox;
    @FXML private ScrollPane chatScroll;

    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        model.sendMessage(message);
        messageField.clear();
    }

    // 🆕 Ny metod: välj och visa en bild
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en bild att skicka");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Bildfiler", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // För Windows – öppna i Bilder-mappen
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Pictures"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            addImageBubble(selectedFile, true);
            // 💡 Här kan du senare lägga till kod för att faktiskt skicka bilden via ntfy
            // (ex. konvertera till Base64 och skicka som text)
        }
    }

    // 🆕 Ny metod: lägg till bildbubbla
    private void addImageBubble(File imageFile, boolean isSentByUser) {
        Image image = new Image(imageFile.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("image-view");

        Label timeLabel = new Label(
                Instant.now().atZone(ZoneId.systemDefault()).toLocalTime().toString().substring(0, 5)
        );
        timeLabel.getStyleClass().add("timestamp");

        VBox content = new VBox(imageView, timeLabel);
        content.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        content.setSpacing(3);

        HBox container = new HBox(content);
        container.setPadding(new Insets(5));
        container.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatBox.getChildren().add(container);
    }

    // ✅ uppdatera så att även tid visas på textbubblor
    private void addMessageBubble(String text, boolean isSentByUser, long timestamp) {
        if (text == null || text.isBlank()) return;

        String timeString = java.time.Instant.ofEpochSecond(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime().toString().substring(0, 5);

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.getStyleClass().add(isSentByUser ? "sent-message" : "received-message");

        Label timeLabel = new Label(timeString);
        timeLabel.getStyleClass().add("timestamp");

        VBox bubbleBox = new VBox(messageLabel, timeLabel);
        bubbleBox.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox container = new HBox(bubbleBox);
        container.setPadding(new Insets(5));
        container.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatBox.getChildren().add(container);
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
                        boolean sentByUser = msg.topic().equals("MartinsTopic");
                        addMessageBubble(msg.message(), sentByUser, msg.time());
                    }
                }
            }
        });

        chatBox.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1.0));
    }

    public void sendMessage(ActionEvent actionEvent) {
        handleSend();
    }

    public void showStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}
