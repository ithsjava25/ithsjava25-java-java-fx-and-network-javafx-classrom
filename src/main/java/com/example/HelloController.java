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
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField messageField;

    @FXML
    private VBox chatBox;

    @FXML
    private ScrollPane chatScroll;

    private final String topic = "chatroom"; // kan ändras om du vill

    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // Lägg till användarens meddelande i chatten direkt
        addMessageBubble("Du: " + message, true);

        // Skicka meddelandet till servern
        model.sendMessage(message);

        messageField.clear();
    }

    private void addMessageBubble(String text, boolean isUser) {
        if (text == null || text.isBlank()) {
            return;
        }
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(400);
        bubble.getStyleClass().add(isUser ? "user-bubble" : "bot-bubble");

        HBox container = new HBox(bubble);
        container.setPadding(new Insets(5));
        container.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatBox.getChildren().add(container);
    }

    @FXML
    private void initialize() {
        // När nya meddelanden kommer in, uppdatera UI
        // Hantera Enter-tangenten i textfältet
        if (messageField != null) {
            messageField.setOnAction(e -> handleSend());
        }
        messageView.setItems(model.getMessages());
        // Scrolla automatiskt till botten när nytt meddelande läggs till
        chatBox.heightProperty().addListener((observable, oldValue, newValue) ->
                chatScroll.setVvalue(1.0));
    }
    public void showStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}
