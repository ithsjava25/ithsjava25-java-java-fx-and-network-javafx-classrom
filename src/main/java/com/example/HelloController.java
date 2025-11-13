package com.example;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    private ListView<NtfyMessageDto> messageView;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField messageField;

    @FXML
    private VBox chatBox;

    @FXML
    private ScrollPane chatScroll;

    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        model.sendMessage(message);
        messageField.clear();
    }

    /**
     * Lägger till en meddelandebubbla i chatten.
     * @param text innehållet i bubblan
     * @param isSentByUser om det är användarens eget meddelande
     */
    private void addMessageBubble(String text, boolean isSentByUser, long timestamp) {
        if (text == null || text.isBlank()) return;

        // Format för tid (ex: 14:32)
        String timeString = java.time.Instant.ofEpochSecond(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .toString()
                .substring(0, 5);

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.getStyleClass().add(isSentByUser ? "sent-message" : "received-message");

        Label timeLabel = new Label(timeString);
        timeLabel.getStyleClass().add("timestamp");
        timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");

        VBox bubbleBox = new VBox(messageLabel, timeLabel);
        bubbleBox.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox container = new HBox(bubbleBox);
        container.setPadding(new Insets(5));
        container.setAlignment(isSentByUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatBox.getChildren().add(container);
    }


    @FXML
    private void initialize() {
        // Hantera Enter-tangenten i textfältet
        if (messageField != null) {
            messageField.setOnAction(e -> handleSend());
        }

        // Lyssna på inkommande meddelanden och uppdatera chatten
        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (NtfyMessageDto msg : change.getAddedSubList()) {
                        // Jämför topic eller clientId här om du vill särskilja egna/andras
                        boolean sentByUser = msg.topic().equals("MartinsTopic");
                        addMessageBubble(msg.message(), sentByUser, msg.time());
                    }
                }
            }
        });

        // Scrolla automatiskt till botten vid nytt meddelande
        chatBox.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1.0));
    }

    public void sendMessage(ActionEvent actionEvent) {
        handleSend();
    }

    public void showStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}
